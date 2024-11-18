package io.github.sspanak.tt9.ime.modes;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.modes.helpers.Cheonjiin;
import io.github.sspanak.tt9.ime.modes.predictions.SyllablePredictions;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class ModeCheonjiin extends InputMode {
	private final static String LOG_TAG = ModeCheonjiin.class.getSimpleName();

	protected boolean disablePredictions = false;
	private final SyllablePredictions predictions;
	@NonNull private String previousJamoSequence = "";


	protected ModeCheonjiin(Context context, SettingsStore settings) {
		super(settings);
		setLanguage(context != null ? LanguageCollection.getLanguage(context, LanguageKind.KOREAN) : null);
		allowedTextCases.add(CASE_LOWER);

		predictions = new SyllablePredictions(settings);
		predictions
			.setLanguage(language)
			.setOnlyExactMatches(true)
			.setMinWords(0)
			.setWordsChangedHandler(this::onPredictions);
	}


	@Override
	public boolean onBackspace() {
		if (!digitSequence.isEmpty()) {
			digitSequence = digitSequence.substring(0, digitSequence.length() - 1);
		}

		return !digitSequence.isEmpty();
	}


	@Override
	public boolean onNumber(int number, boolean hold, int repeat) {
		if (hold) {
			reset();
			digitSequence = String.valueOf(number);
			disablePredictions = true;
			onNumberHold(number);
		} else {
			basicReset();
			disablePredictions = false;
			onNumberPress(number);
		}

		return true;
	}


	protected void onNumberHold(int number) {
		suggestions.add(String.valueOf(number));

		if (number > 1) {
			autoAcceptTimeout = 0;
		} else {
			for (String ch : language.getKeyCharacters(number)) {
				if (!Character.isAlphabetic(ch.codePointAt(0))) {
					suggestions.add(ch);
				}
			}
		}

		// @todo: figure out a way of typing emojis.
	}


	protected void onNumberPress(int number) {
		int rewindAmount = shouldRewindRepeatingNumbers(number);
//		Logger.d(LOG_TAG, "=======> Rewind amount: " + rewindAmount);
		if (rewindAmount > 0) {
			digitSequence = digitSequence.substring(0, digitSequence.length() - rewindAmount);
		}

		digitSequence += String.valueOf(number);

//		Logger.d(LOG_TAG, "=======> digitSequence: " + digitSequence);
	}


	private int shouldRewindRepeatingNumbers(int nextNumber) {
		final int nextChar = nextNumber + '0';
		final int repeatingDigits = digitSequence.length() > 1 && digitSequence.charAt(digitSequence.length() - 1) == nextChar ? Cheonjiin.getRepeatingEndingDigits(digitSequence) : 0;
		final int keyCharsCount = nextNumber == 0 ? 2 : language.getKeyCharacters(nextNumber).size();

		if (repeatingDigits == 0 || keyCharsCount < 2) {
			return 0;
		}

		return keyCharsCount < repeatingDigits + 1 ? repeatingDigits : 0;
	}


	@Override
	public boolean changeLanguage(@Nullable Language newLanguage) {
		return LanguageKind.isKorean(newLanguage);
	}


	@Override
	public void reset() {
		basicReset();
		digitSequence = "";
		previousJamoSequence = "";
		disablePredictions = false;
	}


	protected void basicReset() {
		super.reset();
	}


	@Override
	public void loadSuggestions(String ignored) {
		if (disablePredictions) {
			super.loadSuggestions(ignored);
			return;
		}

		String seq = previousJamoSequence.isEmpty() ? digitSequence : previousJamoSequence;
		Logger.d(LOG_TAG, "=========> Loading suggestions for: " + seq);

		predictions
			.setDigitSequence(seq)
			.load();
	}


	/**
	 * onPredictions
	 * Gets the currently available WordPredictions and sends them over to the external caller.
	 */
	protected void onPredictions() {
		Logger.d(LOG_TAG, "=========> Sending predictions: " + predictions.getList());

		suggestions.clear();
		suggestions.addAll(predictions.getList());

		onSuggestionsUpdated.run();
	}


	private void onReplacementPredictions() {
//		Logger.d(LOG_TAG, "=========> Replacement predictions for: " + digitSequence + " -> " + predictions.getList());
		autoAcceptTimeout = 0;
		onPredictions();
		predictions.setWordsChangedHandler(this::onPredictions);

		Logger.d(LOG_TAG, "========> replacement predictions sent. Loading next. ");
		autoAcceptTimeout = -1;
		loadSuggestions(null);
	}


	@Override
	public boolean containsGeneratedSuggestions() {
		return predictions.containsGeneratedWords();
	}


	@Override
	public void replaceLastLetter() {
		previousJamoSequence = Cheonjiin.stripRepeatingEndingDigits(digitSequence);
		if (previousJamoSequence.isEmpty() || previousJamoSequence.length() == digitSequence.length()) {
			previousJamoSequence = "";
			Logger.w(LOG_TAG, "Cannot strip ending consonant digits from: " + digitSequence + ". Preserving the original sequence and suggestions.");
			return;
		}

		digitSequence = digitSequence.substring(previousJamoSequence.length());

		Logger.d(LOG_TAG, "=======> previousCharSequence: " + previousJamoSequence + " || digitSequence: " + digitSequence);

		predictions.setWordsChangedHandler(this::onReplacementPredictions);
	}


	@Override
	public boolean shouldReplaceLastLetter(int nextKey) {
		boolean yes = Cheonjiin.isThereMediaVowel(digitSequence) && Cheonjiin.isVowelDigit(nextKey);
		Logger.d(LOG_TAG, "========+> is there medial vowel:" + Cheonjiin.isThereMediaVowel(digitSequence) + " + is vowel digit: " + Cheonjiin.isVowelDigit(nextKey));

		if (yes) {
			Logger.d(LOG_TAG, "========+> should preserve last consonant: " + digitSequence + " + " + nextKey);
		}

		return yes;
	}


	/**
	 * shouldAcceptPreviousSuggestion
	 * Used for analysis before processing the incoming pressed key.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) {
		return !digitSequence.isEmpty() && hold;
	}


	/**
	 * shouldAcceptPreviousSuggestion
	 * Used for analysis after loading the suggestions.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(String unacceptedText) {
		return !digitSequence.isEmpty() && predictions.noDbWords() && Cheonjiin.endsWithDashVowel(digitSequence);
	}


	@Override
	public void onAcceptSuggestion(@NonNull String word, boolean preserveWordList) {
		String digitSequenceStash = "";

		boolean mustReload = false;
		if (predictions.noDbWords() && digitSequence.length() >= 2) {
			digitSequenceStash = digitSequence.substring(digitSequence.length() - 1);
			mustReload = true;
		} else if (!previousJamoSequence.isEmpty()) {
			digitSequenceStash = digitSequence;
		}

		reset();

		digitSequence = digitSequenceStash;
		if (mustReload) {
			loadSuggestions(null);
		}
	}


	@Override
	protected boolean nextSpecialCharacters() {
		// @todo: This messes up the character order. Sort it out without breaking the descendants.
		return super.nextSpecialCharacters();
	}


	@Override
	public int getId() {
		return MODE_PREDICTIVE;
	}


	@NonNull
	@Override
	public String toString() {
		return language.getName();
	}
}
