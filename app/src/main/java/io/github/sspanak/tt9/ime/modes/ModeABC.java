package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.Characters;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class ModeABC extends InputMode {
	private final SettingsStore settings;

	public int getId() { return MODE_ABC; }

	private boolean shouldSelectNextLetter = false;

	ModeABC(SettingsStore settings, Language lang) {
		this.settings = settings;
		changeLanguage(lang);
	}

	@Override
	public boolean onNumber(int number, boolean hold, int repeat) {
		if (hold) {
			reset();
			suggestions.add(language.getKeyNumber(number));
			autoAcceptTimeout = 0;
		} else if (repeat > 0) {
			shouldSelectNextLetter = true;
			autoAcceptTimeout = settings.getAbcAutoAcceptTimeout();
		} else {
			reset();
			suggestions.addAll(language.getKeyCharacters(number));
			autoAcceptTimeout = settings.getAbcAutoAcceptTimeout();
		}

		return true;
	}

	@Override
	protected String adjustSuggestionTextCase(String word, int newTextCase) {
		return newTextCase == CASE_UPPER ? word.toUpperCase(language.getLocale()) : word.toLowerCase(language.getLocale());
	}

	@Override
	protected boolean nextSpecialCharacters() {
		return
			suggestions.size() > 0 && (
				suggestions.get(0).equals(language.getKeyCharacters(0).get(0)) ||
				suggestions.get(0).equals(Characters.Currency.get(0))
			)
			&& super.nextSpecialCharacters();
	}

	@Override
	public void changeLanguage(Language language) {
		super.changeLanguage(language);

		allowedTextCases.clear();
		allowedTextCases.add(CASE_LOWER);
		if (language.hasUpperCase()) {
			allowedTextCases.add(CASE_UPPER);
		}

		KEY_CHARACTERS.get(0).add(language.getKeyCharacters(0, true));
		KEY_CHARACTERS.get(0).add(Characters.Currency);
	}

	@Override public int getSequenceLength() { return 1; }

	@Override public boolean shouldAcceptPreviousSuggestion() { return autoAcceptTimeout == 0 || !shouldSelectNextLetter; }
	@Override public boolean shouldSelectNextSuggestion() { return shouldSelectNextLetter; }

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

		String langCode = "";
		if (language.isLatinBased() || language.isCyrillic()) {
			// There are many languages written using the same alphabet, so if the user has enabled multiple,
			// make it clear which one is it, by appending the country code to "ABC" or "АБВ".
			langCode = language.getLocale().getCountry();
			langCode = langCode.isEmpty() ? language.getLocale().getLanguage() : langCode;
			langCode = langCode.isEmpty() ? language.getName() : langCode;
			langCode = " / " + langCode;
		}
		String modeString =  language.getAbcString() + langCode.toUpperCase();

		return (textCase == CASE_LOWER) ? modeString.toLowerCase(language.getLocale()) : modeString.toUpperCase(language.getLocale());
	}
}
