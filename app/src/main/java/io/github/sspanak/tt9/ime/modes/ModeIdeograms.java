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
	private static final String LOG_TAG = ModeIdeograms.class.getSimpleName();


	protected ModeIdeograms(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
	}


	@Override public void determineNextWordTextCase() {}
	@Override protected String adjustSuggestionTextCase(String word, int newTextCase) { return word; }


	@Override
	protected void initPredictions() {
		predictions = new IdeogramPredictions(settings, textField);
		predictions.setWordsChangedHandler(this::onPredictions);

		// @todo: implement lazy displaying of the predictions when they are more than 20
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
		if (language.hasTranscriptionsEmbedded()) {
			((IdeogramPredictions) predictions).stripTranscriptions();
		}
		// @todo: when filtering is on, keep only the latin letters instead

		super.onPredictions();
	}


	@Override
	public void onAcceptSuggestion(@NonNull String currentWord, boolean preserveWords) {
		if (currentWord.isEmpty() || new Text(currentWord).isNumeric()) {
			reset();
			Logger.i(LOG_TAG, "Current word is empty or numeric. Nothing to accept.");
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


	@Override
	public boolean setWordStem(String newStem, boolean exact) {
		// @todo: implement filtering by latin letters
		return false;
	}
}
