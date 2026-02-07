package io.github.sspanak.tt9.ime;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.mindReading.MindReader;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.ime.helpers.SuggestionOps;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.chars.Characters;
import io.github.sspanak.tt9.util.sys.Clipboard;

abstract public class SuggestionHandler extends TypingHandler {
	@Nullable private Handler suggestionHandler;
	@NonNull protected MindReader mindReader = new MindReader();


	@Override
	protected void onInit() {
		super.onInit();
		mindReader = new MindReader(settings, executor);
	}


	@Override
	protected void onFinishTyping() {
		if (suggestionHandler != null) {
			suggestionHandler.removeCallbacksAndMessages(null);
			suggestionHandler = null;
		}

		mindReader.clearContext();
		super.onFinishTyping();
	}


	private String onAcceptPreviousSuggestion() {
		final int lastWordLength = InputModeKind.isABC(mInputMode) ? 1 : mInputMode.getSequenceLength() - 1;
		String lastWord = suggestionOps.getCurrent(mLanguage, lastWordLength);
		if (Characters.PLACEHOLDER.equals(lastWord)) {
			lastWord = "";
		}

		suggestionOps.commitCurrent(false, true);
		mInputMode.onAcceptSuggestion(lastWord, true);
		final String beforeCursor = autoCorrectSpace(
			lastWord,
			textField.getSurroundingStringForAutoAssistance(settings, mInputMode),
			false,
			mInputMode.getFirstKey()
		)[0];
		mInputMode.determineNextWordTextCase(beforeCursor, -1);
		mindReader.setContext(mInputMode, mLanguage, beforeCursor, lastWord);

		return beforeCursor;
	}


	@Override
	protected void onAcceptSuggestionsDelayed(String word) {
		onAcceptSuggestionManually(word, -1);
		forceShowWindow();
	}


	protected void onAcceptSuggestionManually(String word, int fromKey) {
		mInputMode.onAcceptSuggestion(word);
		if (Clipboard.contains(word)) {
			Clipboard.copy(this, word);
		}

		if (!word.isEmpty()) {
			String beforeCursor = autoCorrectSpace(
				word,
				textField.getSurroundingStringForAutoAssistance(settings, mInputMode),
				true,
				fromKey
			)[0];
			updateShiftState(beforeCursor, true, false);
			resetKeyRepeat();
			getMagicSuggestions(beforeCursor, word, true);
		}

		if (!Characters.getSpace(mLanguage).equals(word)) {
			waitForSpaceTrimKey();
		}
	}


	@NonNull
	@Override
	public SuggestionOps getSuggestionOps() {
		return suggestionOps;
	}


	/**
	 * Ask the InputMode to load suggestions for the current state. No action is taken if the dictionary
	 * is still loading. Note that onComplete is called even if the loading was skipped.
	 */
	@Override
	protected void getSuggestions(@Nullable String currentWord, @Nullable Runnable onComplete) {
		if (InputModeKind.isPredictive(mInputMode) && DictionaryLoader.getInstance(this).isRunning()) {
			mInputMode.reset();
			UI.toastShortSingle(this, R.string.dictionary_loading_please_wait);
			if (onComplete != null) {
				onComplete.run();
			}
		} else {
			mInputMode
				.setOnSuggestionsUpdated(() -> handleSuggestionsFromThread(onComplete))
				.loadSuggestions(currentWord == null ? suggestionOps.getCurrent() : currentWord);
		}
	}


	protected void handleSuggestionsFromThread() {
		handleSuggestionsFromThread(null);
	}


	protected void handleSuggestionsFromThread(@Nullable Runnable onComplete) {
		if (suggestionHandler == null) {
			suggestionHandler = new Handler(Looper.getMainLooper());
		} else {
			suggestionHandler.removeCallbacksAndMessages(null);
		}
		suggestionHandler.post(() -> handleSuggestions(onComplete));
	}


	protected void handleSuggestions(@Nullable Runnable onComplete) {
		// Second pass, analyze the available suggestions and decide if combining them with the
		// last key press makes up a compound word like: (it)'s, (I)'ve, l'(oiseau), or it is
		// just the end of a sentence, like: "word." or "another?"
		String beforeCursor = null;
		if (mInputMode.shouldAcceptPreviousSuggestion(suggestionOps.getCurrent())) {
			beforeCursor = onAcceptPreviousSuggestion();
		}

		final ArrayList<String> suggestions = mInputMode.getSuggestions();
		final boolean noSuggestionsBefore = suggestionOps.isEmpty();
		suggestionOps.set(suggestions, mInputMode.getRecommendedSuggestionIdx(), mInputMode.containsGeneratedSuggestions());

		// either accept the first one automatically (when switching from punctuation to text
		// or vice versa), or schedule auto-accept in N seconds (in ABC mode)
		if (suggestionOps.scheduleDelayedAccept(mInputMode.getAutoAcceptTimeout())) {
			return;
		}

		// We have not accepted anything yet, which means the user is composing a word.
		// put the first suggestion in the text field, but cut it off to the length of the sequence
		// (the count of key presses), for a more intuitive experience.
		String trimmedWord;

		if (InputModeKind.isRecomposing(mInputMode)) {
			// highlight the current letter, when editing a word
			trimmedWord = mInputMode.getWordStem() + suggestionOps.getCurrent();
			appHacks.setComposingTextPartsWithHighlightedJoining(trimmedWord, mInputMode.getRecomposingSuffix());
		} else {
			// or highlight the stem, when filtering
			trimmedWord = suggestionOps.getCurrent(mLanguage, mInputMode.getSequenceLength());
			appHacks.setComposingTextWithHighlightedStem(trimmedWord, mInputMode.getWordStem(), mInputMode.isStemFilterFuzzy());
		}

		onAfterSuggestionsHandled(onComplete, beforeCursor, trimmedWord, suggestions.isEmpty(), noSuggestionsBefore);
	}


	private void onAfterSuggestionsHandled(@Nullable Runnable callback, @Nullable String beforeCursor, @Nullable String trimmedWord, boolean noSuggestions, boolean noSuggestionsBefore) {
		final String shiftStateContext = beforeCursor != null ? beforeCursor + trimmedWord : trimmedWord;
		if (noSuggestions) {
			updateShiftStateDebounced(shiftStateContext, true, false);
		} else {
			updateShiftStateDebounced(shiftStateContext, false, true);
		}

		forceShowWindow();

			// @todo: here get completions for the current word, instead of the next words for it.
//		if (noSuggestionsBefore && !noSuggestions && !mInputMode.containsSpecialChars()) {

//			mindReader.guess(
//				mLanguage,
//				beforeCursor == null ? textField.getSurroundingStringForAutoAssistance(settings, mInputMode)[0] : beforeCursor + trimmedWord,
//				false
//			);
//		}

		if (callback != null) {
			callback.run();
		}
	}


	@Override
	protected boolean clearMagicContext() {
		return mindReader.clearContext();
	}


	@Override
	protected void setMagicContext(@NonNull String beforeCursor, @Nullable String lastWord) {
		mindReader.setContext(mInputMode, mLanguage, beforeCursor, lastWord);
	}


	@Override
	protected void getMagicSuggestions(@NonNull String beforeCursor, @Nullable String lastWord, boolean saveContext) {
		mindReader.guess(mInputMode, mLanguage, beforeCursor, lastWord, saveContext, this::handleMagicSuggestions);
	}


	private void handleMagicSuggestions(ArrayList<String> suggestions) {
		Logger.d("LOG", "=========> " + suggestions);
	}
}
