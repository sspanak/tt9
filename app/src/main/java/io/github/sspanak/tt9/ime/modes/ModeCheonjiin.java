package io.github.sspanak.tt9.ime.modes;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.modes.predictions.SyllablePredictions;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ModeCheonjiin extends InputMode {
	protected boolean disablePredictions = false;
	private final SyllablePredictions predictions;


	protected ModeCheonjiin(Context context, SettingsStore settings) {
		super(settings);
		setLanguage(context != null ? LanguageCollection.getLanguage(context, LanguageKind.KOREAN) : null);
		allowedTextCases.add(CASE_LOWER);

		predictions = new SyllablePredictions(settings);
		predictions
			.setOnlyExactMatches(true)
			.setMinWords(1)
			.setMaxWords(2)
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
		if (onSameNumbersRewind(number)) {
			digitSequence = String.valueOf(number);
		} else {
			digitSequence += String.valueOf(number);
		}
	}


	private boolean onSameNumbersRewind(int number) {
		int nextChar = number + '0';

		if (
			(digitSequence.length() == 2 && digitSequence.codePointAt(0) == nextChar) ||
			(digitSequence.length() == 3 && digitSequence.codePointAt(1) == nextChar && digitSequence.codePointAt(2) == nextChar)
		) {
			return language.getKeyCharacters(number).size() < digitSequence.length() + 1;
		}

		return false;
	}


	@Override
	public boolean changeLanguage(@Nullable Language newLanguage) {
		return LanguageKind.isKorean(newLanguage);
	}


	@Override
	public void reset() {
		basicReset();
		digitSequence = "";
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

		predictions
			.setDigitSequence(digitSequence)
			.setLanguage(language)
			.load();
	}


	/**
	 * onPredictions
	 * Gets the currently available WordPredictions and sends them over to the external caller.
	 */
	protected void onPredictions() {
		suggestions.clear();
		suggestions.addAll(predictions.getList());

		onSuggestionsUpdated.run();
	}


	@Override
	public boolean containsGeneratedSuggestions() {
		return predictions.containsGeneratedWords();
	}


	/**
	 * shouldAcceptPreviousSuggestion
	 * Used for analysis before processing the incoming pressed key.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) {
		// @todo: nextKey is a vowel key:
		// 1. cut the current word up to the last vowel digit and accept it.
		// 2. use the last consonant digits of the previous word (if any) as the start of a new character.

		return !digitSequence.isEmpty() && (hold || predictions.noDbWords());
	}


	/**
	 * shouldAcceptPreviousSuggestion
	 * Used for analysis after loading the suggestions.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(String unacceptedText) {
		return false;
	}


	@Override
	public void onAcceptSuggestion(@NonNull String word) {
		reset();
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
