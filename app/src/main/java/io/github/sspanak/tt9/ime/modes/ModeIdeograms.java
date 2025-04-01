package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.predictions.IdeogramPredictions;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.TextTools;

public class ModeIdeograms extends ModeWords {
	private static final String LOG_TAG = ModeIdeograms.class.getSimpleName();
	protected String NAME;

	private boolean isFiltering = false;


	protected ModeIdeograms(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
		NAME = super.toString();
	}


	@Override protected String adjustSuggestionTextCase(String word, int newTextCase) { return word; }
	@Override public void determineNextWordTextCase() {}


	@Override
	public boolean changeLanguage(@Nullable Language newLanguage) {
		if (newLanguage != null && !newLanguage.isTranscribed() || LanguageKind.isKorean(newLanguage)) {
			return false;
		}

		setLanguage(newLanguage);
		return true;
	}


	@Override
	public void reset() {
		super.reset();
		isFiltering = false;
	}

	/******************************* LOAD SUGGESTIONS *********************************/

	@Override
	protected void initPredictions() {
		predictions = new IdeogramPredictions(settings, textField);
		predictions.setWordsChangedHandler(this::onPredictions);
	}


	@Override
	protected void onPredictions() {
		if (language.hasTranscriptionsEmbedded()) {
			if (isFiltering) {
				((IdeogramPredictions) predictions).stripNativeWords();
			} else {
				((IdeogramPredictions) predictions).stripTranscriptions();
			}
		}

		if (!isFiltering) {
			// We can reorder by pairs only after stripping the transcriptions, if any.
			// Otherwise, the input field words will not match with any pair.
			((IdeogramPredictions) predictions).orderByPairs();
		}

		super.onPredictions();
	}

	/******************************* ACCEPT WORDS *********************************/

	@Override
	public void onAcceptSuggestion(@NonNull String currentWord, boolean preserveWords) {
		if (currentWord.isEmpty() || new Text(currentWord).isNumeric()) {
			reset();
			Logger.i(LOG_TAG, "Current word is empty or numeric. Nothing to accept.");
			return;
		}

		if (isFiltering) {
			isFiltering = false;
			stem = currentWord;
			loadSuggestions("");
			return;
		}

		try {
			String latinWord = ((IdeogramPredictions) predictions).getTranscription(currentWord);
			String digits = language.getDigitSequenceForWord(latinWord);
			((IdeogramPredictions) predictions).onAcceptTranscription(currentWord, latinWord, digits);
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Failed incrementing priority of word: '" + currentWord + "'. " + e.getMessage());
		}

		int len = digitSequence.length();
		if (preserveWords && len >= 2) {
			digitSequence = digitSequence.substring(len - 1);
			loadSuggestions("");
		} else {
			reset();
		}
	}


	@Override
	public boolean shouldAcceptPreviousSuggestion(String s) {
		return
			!digitSequence.isEmpty()
			&& predictions.noDbWords()
			&& !digitSequence.equals(EMOJI_SEQUENCE)
			&& !digitSequence.equals(PUNCTUATION_SEQUENCE)
			&& !digitSequence.equals(SPECIAL_CHAR_SEQUENCE);
	}


	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) {
		if (digitSequence.isEmpty()) {
			return false;
		}

		if (super.shouldAcceptPreviousSuggestion(nextKey, hold)) {
			return true;
		}

		String nextSequence = digitSequence + (char)(nextKey + '0');

		return
			TextTools.containsOtherThan1(nextSequence)
			&& (
				nextSequence.endsWith(EMOJI_SEQUENCE) || nextSequence.startsWith(EMOJI_SEQUENCE) ||
				nextSequence.endsWith(PUNCTUATION_SEQUENCE) || nextSequence.startsWith(PUNCTUATION_SEQUENCE)
			);
	}


	/**
	 * When we want to filter by a Latin transcription, we must have discarded it from the text field,
	 * then give it to this method. It will filter the suggestions and show only the ones that match
	 * the given Latin word.
	 */
	@Override
	public void onReplaceSuggestion(@NonNull String word) {
		if (word.isEmpty() || new Text(word).isNumeric()) {
			reset();
			Logger.i(LOG_TAG, "Can not replace an empty or numeric word.");
			return;
		}

		isFiltering = false;
		stem = word;
		loadSuggestions("");
	}


	/**
	 * This should be called before accepting a word. It says whether we should discard the current
	 * word. Discarding it means we want to erase it from the text field and instead display a
	 * filtered list of suggestions that matches the word. If we don't discard it, usually we should
	 * accept it.
	 */
	@Override
	public boolean shouldReplacePreviousSuggestion() {
		return isFiltering;
	}

	/********************************* FILTERING *********************************/

	@Override
	public boolean clearWordStem() {
		if (!supportsFiltering()) {
			return false;
		}

		isFiltering = false;
		stem = "";
		return true;
	}


	@Override
	public boolean setWordStem(String newStem, boolean fromScrolling) {
		if (!supportsFiltering()) {
			return false;
		}

		if (!fromScrolling) {
			isFiltering = true;
		} else if (isFiltering) {
			stem = newStem;
		}

		return true;
	}


	@Override
	public boolean supportsFiltering() {
		return language.hasTranscriptionsEmbedded();
	}


	@Override
	public boolean isStemFilterFuzzy() {
		return isFiltering;
	}


	@Override public void onCursorMove(@NonNull String word) {
		isFiltering = false;
		super.onCursorMove(word);
	}

	/********************************* NAME *********************************/

	@NonNull
	@Override
	public String toString() {
		return NAME;
	}
}
