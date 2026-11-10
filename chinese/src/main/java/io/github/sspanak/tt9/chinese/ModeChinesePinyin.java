package io.github.sspanak.tt9.chinese;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.ModePinyin;
import io.github.sspanak.tt9.ime.modes.helpers.PinyinSequenceEditor;
import io.github.sspanak.tt9.ime.modes.predictions.IdeogramPredictions;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

public class ModeChinesePinyin extends ModePinyin {
	private final TextField editingTextField;
	@Nullable private volatile PinyinSequenceEditor sequenceEditor;
	private String originalSequence = "";
	private String originalReading = "";
	private String initialEditableReading = "";
	private final PinyinEditState editState = new PinyinEditState();
	private long sequenceQueryVersion;
	@Nullable private Handler sequenceEditHandler;
	@Nullable private Runnable sequenceQueryTimeout;
	@Nullable private Runnable spellingQueryTimeout;


	public ModeChinesePinyin(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
		editingTextField = textField;
		// Base construction initializes predictions before the requested language is assigned.
		if (LanguageKind.isChinesePinyin(lang)) {
			predictions = new ChinesePredictions(settings);
			predictions.setWordsChangedHandler(this::onPredictions);
		}
	}


	@Override
	public boolean validateLanguage(@Nullable Language newLanguage) {
		return LanguageKind.isChinesePinyin(newLanguage);
	}

	@Override public boolean supportsExpandedSuggestions() { return true; }


	public boolean isPinyinSequenceEditing() { return sequenceEditor != null; }
	public String getPinyinSequencePreview() {
		PinyinSequenceEditor editor = sequenceEditor;
		return editor == null ? "" : editingTextField.supportsComposingCursor() ? editor.getReading() : editor.getPreview();
	}
	public int getPinyinSequenceCursor() { return sequenceEditor != null ? sequenceEditor.getCursor() : -1; }


	/** The second Filter press edits the reading currently selected in the phonetic list. */
	public boolean beginPinyinSequenceEdit(String selectedReading) {
		if (!LanguageKind.isChinesePinyin(language) || sequenceEditor != null || !isStemFilterFuzzy() || digitSequence.isEmpty()) {
			return false;
		}
		try {
			sequenceEditor = PinyinEditReading.create(digitSequence, selectedReading, settings.getChineseFuzzyOptions());
		} catch (IllegalArgumentException e) {
			return false;
		}
		originalSequence = digitSequence;
		originalReading = selectedReading;
		initialEditableReading = sequenceEditor.getReading();
		digitSequence = sequenceEditor.getDigits();
		if (sequenceEditHandler == null) sequenceEditHandler = new Handler(Looper.getMainLooper());
		stem = "";
		publishSequenceEdit();
		return true;
	}


	public boolean movePinyinSequenceCursor(int direction) {
		if (sequenceEditor == null) {
			return false;
		}
		sequenceEditor.moveCursor(direction);
		publishSequenceEdit();
		return true;
	}


	@Override
	public boolean setPinyinSequenceCursor(int offset) {
		PinyinSequenceEditor editor = sequenceEditor;
		if (editor == null) return false;
		if (editor.getCursor() != offset) {
			editor.setCursor(offset);
			publishSequenceEdit();
		}
		return true;
	}


	public boolean finishPinyinSequenceEdit() {
		if (sequenceEditor == null) {
			return false;
		}
		// A digit awaiting resolution (or a spelling with no dictionary match) is still an
		// editable reading. Do not turn it into an empty candidate list or commit it as ASCII.
		if (!editState.requestFinish(!sequenceEditor.getDigits().isEmpty(), sequenceEditor.hasResolvedReading())) {
			if (!editState.isQueryPending()) showUnresolvedReading();
			return false;
		}
		String reading = sequenceEditor.getResolvedReading();
		if (!reading.isEmpty() && sequenceEditor.getReading().equals(initialEditableReading)) reading = originalReading;
		invalidateSequenceQuery();
		sequenceEditor = null;
		editingTextField.setComposingCursor(-1);
		originalSequence = originalReading = initialEditableReading = "";
		returnToChinese(reading);
		return true;
	}


	public boolean cancelPinyinSequenceEdit() {
		if (sequenceEditor == null) {
			return false;
		}
		digitSequence = originalSequence;
		String reading = originalReading;
		invalidateSequenceQuery();
		sequenceEditor = null;
		editingTextField.setComposingCursor(-1);
		originalSequence = originalReading = initialEditableReading = "";
		returnToChinese(reading);
		return true;
	}


	private void returnToChinese(String reading) {
		editState.beginReturn();
		super.clearWordStem();
		if (digitSequence.isEmpty()) {
			reset();
			onSuggestionsUpdated.run();
		} else if (!reading.isEmpty()) {
			super.onReplaceSuggestion(reading);
		} else {
			super.loadSuggestions("");
		}
	}


	@Override public boolean setWordStem(String newStem, boolean fromScrolling) {
		// Reopening the spelling list starts with the whole current key sequence. The reading
		// selected for the previous Han conversion must not hide its phonetic alternatives.
		if (!fromScrolling && sequenceEditor == null) super.clearWordStem();
		return super.setWordStem(newStem, fromScrolling);
	}


	@Override
	public boolean onNumber(int number, boolean hold, int repeat, @NonNull String[] surroundingChars) {
		if (sequenceEditor == null) {
			return super.onNumber(number, hold, repeat, surroundingChars);
		}
		sequenceEditor.insertDigit(number);
		digitSequence = sequenceEditor.getDigits();
		return true;
	}


	@Override
	public boolean onBackspace() {
		if (sequenceEditor == null) {
			return super.onBackspace();
		}
		sequenceEditor.deleteBeforeCursor();
		digitSequence = sequenceEditor.getDigits();
		return true; // At the left boundary, do not delete already committed host text.
	}


	@Override
	public void loadSuggestions(String currentWord) {
		PinyinSequenceEditor editor = sequenceEditor;
		if (editor == null) {
			if (isStemFilterFuzzy()) {
				editState.beginSpellings();
				long revision = editState.getRevision();
				if (sequenceEditHandler == null) sequenceEditHandler = new Handler(Looper.getMainLooper());
				if (spellingQueryTimeout != null) sequenceEditHandler.removeCallbacks(spellingQueryTimeout);
				spellingQueryTimeout = () -> {
					if (!editState.isSpellingsPending() || revision != editState.getRevision()) return;
					// A cancelled or timed-out query must not trap subsequent keys indefinitely.
					suggestions = new ArrayList<>();
					editState.predictionsReady();
					onSuggestionsUpdated.run();
					showUnresolvedReading();
				};
				sequenceEditHandler.postDelayed(spellingQueryTimeout, SettingsStore.SLOW_QUERY_TIMEOUT);
			}
			super.loadSuggestions(currentWord);
			return;
		}
		publishSequenceEdit();
		if (digitSequence.isEmpty()) {
			return;
		}
		// Editing queries must not overwrite the normal Han candidate state after finishing.
		IdeogramPredictions query = new ChinesePredictions(settings).setCompletionsAllowed(false);
		long version = ++sequenceQueryVersion;
		Handler handler = sequenceEditHandler;
		editState.beginQuery();
		if (sequenceQueryTimeout != null) handler.removeCallbacks(sequenceQueryTimeout);
		sequenceQueryTimeout = () -> {
			if (sequenceEditor != editor || version != sequenceQueryVersion) return;
			boolean finishRequested = editState.isFinishPending();
			editState.completeQuery(false);
			publishSequenceEdit();
			if (finishRequested) showUnresolvedReading();
		};
		handler.postDelayed(sequenceQueryTimeout, SettingsStore.SLOW_QUERY_TIMEOUT);
		query.setWordsChangedHandler(() -> handler.post(() -> {
			if (sequenceEditor != editor || version != sequenceQueryVersion) {
				return;
			}
			query.stripNativeWords();
			editor.resolveReading(query.getList());
			if (sequenceQueryTimeout != null) handler.removeCallbacks(sequenceQueryTimeout);
			boolean finishRequested = editState.isFinishPending();
			if (editState.completeQuery(editor.hasResolvedReading())) finishPinyinSequenceEdit();
			else {
				publishSequenceEdit();
				if (finishRequested) showUnresolvedReading();
			}
		}));
		query.setStem("").setInputWord("").setIsStemFuzzy(false)
			.setDigitSequence(digitSequence).setLanguage(language).load();
	}

	@Override protected void onPredictions() {
		if (sequenceEditor == null) {
			if (spellingQueryTimeout != null && sequenceEditHandler != null) sequenceEditHandler.removeCallbacks(spellingQueryTimeout);
			editState.predictionsReady();
			super.onPredictions();
		}
	}


	@Override public long getPinyinSuggestionsRevision() { return editState.getReadyRevision(); }
	@Override public void onPinyinSuggestionsShown(long revision) {
		if (sequenceEditor == null) {
			// Keep protecting the old preview until its replacement has actually reached the UI.
			// Afterwards a normal spelling such as XIAN is no longer an editing preview token.
			editState.suggestionsShown(revision);
		}
	}


	private void publishSequenceEdit() {
		PinyinSequenceEditor editor = sequenceEditor;
		if (editor != null) {
			autoAcceptTimeout = -1;
			editingTextField.setComposingCursor(editor.getCursor());
			String preview = getPinyinSequencePreview();
			editState.publish(preview);
			suggestions = new ArrayList<>(Collections.singletonList(preview));
			onSuggestionsUpdated.run();
		}
	}


	private void showUnresolvedReading() {
		Toast.makeText(settings.getContext(), R.string.chinese_pinyin_unresolved, Toast.LENGTH_SHORT).show();
	}


	private void invalidateSequenceQuery() {
		sequenceQueryVersion++;
		if (sequenceEditHandler != null) {
			sequenceEditHandler.removeCallbacksAndMessages(null);
		}
	}


	/** Includes the short transition after editing, before Han candidates replace the preview. */
	public boolean isPinyinEditPreview(@Nullable String word) {
		return editState.isPreview(word);
	}
	@Override public boolean isPinyinSequenceTransitionPending() { return editState.isTransitionPending(); }


	@Override public int getSequenceLength() {
		if (sequenceEditor != null) return getPinyinSequencePreview().length();
		int length = super.getSequenceLength();
		if (isStemFilterFuzzy()) for (String reading : suggestions) length = Math.max(length, reading.length());
		return length;
	}
	@Override public boolean isTyping() { return sequenceEditor != null || super.isTyping(); }
	@Override public boolean containsGeneratedSuggestions() { return sequenceEditor == null && super.containsGeneratedSuggestions(); }
	@Override public boolean shouldSelectNextSuggestion() { return sequenceEditor == null && super.shouldSelectNextSuggestion(); }
	@Override public boolean shouldAcceptPreviousSuggestion(String word) { return sequenceEditor == null && !editState.isTransitionPending() && !isPinyinEditPreview(word) && super.shouldAcceptPreviousSuggestion(word); }
	@Override public boolean shouldAcceptPreviousSuggestion(String word, int key, boolean hold) { return sequenceEditor == null && !editState.isTransitionPending() && !isPinyinEditPreview(word) && super.shouldAcceptPreviousSuggestion(word, key, hold); }
	@Override public boolean shouldReplacePreviousSuggestion(@Nullable String word) { return sequenceEditor != null || isPinyinEditPreview(word) || super.shouldReplacePreviousSuggestion(word); }


	@Override
	public boolean onReplaceSuggestion(@NonNull String word) {
		if (sequenceEditor != null) {
			return finishPinyinSequenceEdit();
		}
		return isPinyinEditPreview(word) || super.onReplaceSuggestion(word);
	}


	@Override
	public void reset() {
		invalidateSequenceQuery();
		sequenceEditor = null;
		if (editingTextField != null) editingTextField.setComposingCursor(-1);
		originalSequence = originalReading = initialEditableReading = "";
		if (editState != null) editState.reset();
		super.reset();
	}


	@Override
	protected String getPreferredChar() {
		return Characters.getChar(language, settings.getDoubleZeroChar());
	}

	@NonNull
	@Override
	public String toAccessibilityString(@NonNull Context ctx) {
		return language.getName() + ", 拼音";
	}
}
