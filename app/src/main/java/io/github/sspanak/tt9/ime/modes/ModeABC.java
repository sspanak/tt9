package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

class ModeABC extends InputMode {
	private final ArrayList<ArrayList<String>> KEY_CHARACTERS = new ArrayList<>();

	private boolean shouldSelectNextLetter = false;

	@Override public int getId() { return MODE_ABC; }

	protected ModeABC(SettingsStore settings, Language lang, InputType inputType) {
		super(settings, inputType);
		setLanguage(lang);
	}

	@Override
	public boolean onBackspace() {
		if (suggestions.isEmpty()) {
			return false;
		}

		reset();
		return true;
	}

	@Override
	public boolean onNumber(int number, boolean hold, int repeat) {
		if (hold) {
			reset();
			autoAcceptTimeout = 0;
			digitSequence = String.valueOf(number);
			shouldSelectNextLetter = false;
			suggestions.add(language.getKeyNumeral(number));
		} else if (repeat > 0) {
			autoAcceptTimeout = settings.getAbcAutoAcceptTimeout();
			shouldSelectNextLetter = true;
		} else {
			reset();
			autoAcceptTimeout = settings.getAbcAutoAcceptTimeout();
			digitSequence = String.valueOf(number);
			shouldSelectNextLetter = false;
			suggestions.addAll(KEY_CHARACTERS.size() > number ? KEY_CHARACTERS.get(number) : settings.getOrderedKeyChars(language, number));
			suggestions.add(language.getKeyNumeral(number));
		}

		return true;
	}

	@Override
	protected String adjustSuggestionTextCase(String word, int newTextCase) {
		return newTextCase == CASE_UPPER ? word.toUpperCase(language.getLocale()) : word.toLowerCase(language.getLocale());
	}

	private void refreshSuggestions() {
		if (digitSequence.isEmpty()) {
			suggestions.clear();
		} else {
			onNumber(digitSequence.charAt(0) - '0', false, 0);
		}
	}

	@Override
	public boolean setLanguage(@Nullable Language newLanguage) {
		if (newLanguage != null && !newLanguage.hasABC()) {
			return false;
		}

		super.setLanguage(newLanguage);

		allowedTextCases.clear();
		allowedTextCases.add(CASE_LOWER);
		if (language.hasUpperCase()) {
			allowedTextCases.add(CASE_UPPER);
		}

		KEY_CHARACTERS.clear();
		if (isEmailMode) {
			// Asian punctuation can not be used in email addresses, so we need to use the English locale.
			Language lang = LanguageKind.isCJK(language) ? LanguageCollection.getByLocale("en") : language;
			KEY_CHARACTERS.add(Characters.orderByList(Characters.Email.get(0), settings.getOrderedKeyChars(lang, 0), true));
			KEY_CHARACTERS.add(Characters.orderByList(Characters.Email.get(1), settings.getOrderedKeyChars(lang, 1), true));
		}

		refreshSuggestions();
		shouldSelectNextLetter = true; // do not accept any previous suggestions after loading the new ones

		return true;
	}

	@Override
	public void setSequence(@NonNull String sequence) {
		super.setSequence(sequence);
		refreshSuggestions();
		shouldSelectNextLetter = true;
	}

	@Override public void onAcceptSuggestion(@NonNull String w) { reset(); }
	@Override public boolean shouldAcceptPreviousSuggestion(String w) { return !shouldSelectNextLetter; }
	@Override public boolean shouldSelectNextSuggestion() { return shouldSelectNextLetter; }

	@Override
	public void reset() {
		super.reset();
		digitSequence = "";
		shouldSelectNextLetter = false;
	}

	@NonNull
	@Override
	public String toString() {
		String modeString = language.getAbcString();

		// There are many languages written using the same alphabet, so if the user has
		// enabled multiple ones, make it clear which one is it, by appending the unique
		// country or language code to "ABC" or "АБВ".
		if (LanguageKind.isArabicBased(language) || LanguageKind.isCyrillic(language) || LanguageKind.isHebrew(language) || LanguageKind.isLatinBased(language)) {
			modeString += " / " + language.getCode();
		}

		return (textCase == CASE_LOWER) ? modeString.toLowerCase(language.getLocale()) : modeString.toUpperCase(language.getLocale());
	}
}
