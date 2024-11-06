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
	private SyllablePredictions predictions;


	protected ModeCheonjiin(Context context, SettingsStore settings) {
		super(settings);
		setLanguage(context != null ? LanguageCollection.getLanguage(context, LanguageKind.KOREAN) : null);
		allowedTextCases.add(CASE_LOWER);
		predictions = new SyllablePredictions(settings);
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
		} else {
			digitSequence += String.valueOf(number);
		}

		return true;
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

		suggestions.clear();
		if (!digitSequence.isEmpty()) {
			suggestions.add(digitSequence);
		}

		super.loadSuggestions(ignored);
	}


	@Override
	public boolean shouldAcceptPreviousSuggestion(String unacceptedText) {
		return super.shouldAcceptPreviousSuggestion(unacceptedText);
	}


	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) {
		return super.shouldAcceptPreviousSuggestion(nextKey, hold);
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
