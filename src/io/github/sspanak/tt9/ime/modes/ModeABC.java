package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.Language;

public class ModeABC extends InputMode {
	public int getId() { return MODE_ABC; }

	private boolean shouldSelectNextLetter = false;

	ModeABC(Language lang) {
		changeLanguage(lang);
	}


	@Override
	public boolean onNumber(int number, boolean hold, int repeat) {
		if (hold) {
			reset();
			suggestions.add(String.valueOf(number));
			autoAcceptTimeout = 0;
		} else if (repeat > 0) {
			shouldSelectNextLetter = true;
		} else {
			reset();
			suggestions.addAll(language.getKeyCharacters(number));
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

	@Override
	public void reset() {
		super.reset();
		shouldSelectNextLetter = false;
	}

	@NonNull
	@Override
	public String toString() {
		if (language == null) {
			return textCase == CASE_LOWER ? "abc" : "ABC";
		}

		String langCode = language.getLocale().getCountry();
		langCode = langCode.length() == 0 ? language.getLocale().getLanguage() : langCode;

		String modeString =  language.getAbcString() + " / " + langCode.toUpperCase();

		return (textCase == CASE_LOWER) ? modeString.toLowerCase(language.getLocale()) : modeString.toUpperCase(language.getLocale());
	}
}
