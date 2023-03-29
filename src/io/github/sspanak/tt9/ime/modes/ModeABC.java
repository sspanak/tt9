package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;

public class ModeABC extends InputMode {
	public int getId() { return MODE_ABC; }

	private boolean shouldSelectNextLetter = false;

	ModeABC(Language lang) {
		changeLanguage(lang);
	}


	@Override
	public boolean onNumber(int key, boolean hold, int repeat) {
		shouldSelectNextLetter = false;
		suggestions = language.getKeyCharacters(key);
		word = null;

		if (hold) {
			suggestions = new ArrayList<>();
			word = String.valueOf(key);
		} else if (repeat > 0) {
			shouldSelectNextLetter = true;
		}

		return true;
	}


	@Override
	protected String adjustSuggestionTextCase(String word, int newTextCase) {
		return newTextCase == CASE_UPPER ? word.toUpperCase(language.getLocale()) : word.toLowerCase(language.getLocale());
	}

	@Override
	public void changeLanguage(Language language) {
		super.changeLanguage(language);

		allowedTextCases.clear();
		allowedTextCases.add(CASE_LOWER);
		if (language.hasUpperCase()) {
			allowedTextCases.add(CASE_UPPER);
		}
	}

	@Override final public boolean isABC() { return true; }
	@Override public int getSequenceLength() { return 1; }

	@Override public boolean shouldAcceptCurrentSuggestion(int key, boolean hold, boolean repeat) { return hold || !repeat; }
	@Override public boolean shouldTrackUpDown() { return true; }
	@Override public boolean shouldTrackLeftRight() { return true; }
	@Override public boolean shouldSelectNextSuggestion() {
		return shouldSelectNextLetter;
	}

	@NonNull
	@Override
	public String toString() {
		String modeString = textCase == CASE_LOWER ? "abc" : "ABC";
		if (language != null) {
			String abc = (textCase == CASE_LOWER) ? language.getAbcString().toLowerCase(language.getLocale()) : language.getAbcString().toUpperCase(language.getLocale());

			String langCode = language.getLocale().getCountry();
			langCode = langCode.length() == 0 ? language.getLocale().getLanguage() : langCode;

			modeString = abc + " / " + langCode.toUpperCase();
		}

		return modeString;
	}
}
