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
	// async suggestion handling
	protected boolean disablePredictions = false;
	private final SyllablePredictions predictions;


	protected ModeCheonjiin(Context context, SettingsStore settings) {
		super(settings);
		setLanguage(context != null ? LanguageCollection.getLanguage(context, LanguageKind.KOREAN) : null);
		allowedTextCases.add(CASE_LOWER);

		predictions = new SyllablePredictions(settings);
		predictions.setWordsChangedHandler(this::onPredictions);
	}


	@Override
	public boolean onBackspace() {
		if (!digitSequence.isEmpty()) {
			digitSequence = digitSequence.substring(0, digitSequence.length() - 1);
			return true;
		}

		return false;
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
		if (number > 1) {
			autoAcceptTimeout = 0;
		}

		// @todo: on 0 and 1, add the special chars.
		// @todo: figure out a way of typing emojis.
		suggestions.add(String.valueOf(number));
	}


	protected void onNumberPress(int number) {
		digitSequence += String.valueOf(number);
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

		// @todo: it is not possible to search for sequences that start with "0", because ReadOps.getFactoryWordPositionsQuery() treats them as integers.
		// @todo: search only for exact matches
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
	public int getId() {
		return MODE_PREDICTIVE;
	}


	@NonNull
	@Override
	public String toString() {
		return language.getName();
	}
}
