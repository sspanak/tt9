package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.predictions.IdeogramPredictions;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.TextTools;

public class ModeIdeograms extends ModeWords {
	private static String LOG_TAG = ModeIdeograms.class.getSimpleName();

	protected ModeIdeograms(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
	}

	@Override public void determineNextWordTextCase() {}
	@Override protected String adjustSuggestionTextCase(String word, int newTextCase) { return word; }

	@Override
	protected void initPredictions() {
		predictions = new IdeogramPredictions(settings, textField);
		predictions.setWordsChangedHandler(this::onPredictions);

		// @todo: accept words on space
		// @todo: implement lazy displaying of the predictions when they are more than 20
		// @todo: Switching the language while typing may produce weird results on Android < 7
		// @todo: add frequencies to the dictionary
		// @todo: documentation of the new YAML properties
	}


	@Override
	public boolean changeLanguage(@Nullable Language newLanguage) {
		if (newLanguage != null && !newLanguage.isTranscribed()) {
			return false;
		}

		setLanguage(newLanguage);
		return true;
	}


	@Override
	protected void onPredictions() {
		// @todo: stripping transcriptions should be optional, based on "filterBySound"
		((IdeogramPredictions) predictions).stripTranscriptions();
		// @todo: when filtering is on, keep only the latin letters instead

		super.onPredictions();
	}


	@Override
	public void onAcceptSuggestion(@NonNull String currentWord, boolean preserveWords) {
		String lastDigitSequence = digitSequence;
		reset();
		setWordStem("", true);

		if (currentWord.isEmpty()) {
			Logger.i(LOG_TAG, "Current word is empty. Nothing to accept.");
			return;
		}

		if (TextTools.isGraphic(currentWord) || new Text(currentWord).isNumeric()) {
			return;
		}

		// @todo: increment frequency

		int len = lastDigitSequence.length();
		if (preserveWords && len >= 2) {
			digitSequence = lastDigitSequence.substring(len - 2, len - 1);
			loadSuggestions("");
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


	@Override
	public boolean setWordStem(String newStem, boolean exact) {
		// @todo: implement filtering by latin letters
		return false;
	}
}
