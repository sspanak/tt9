package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.AutoTextCase;
import io.github.sspanak.tt9.ime.modes.helpers.Sequences;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.chars.Characters;

class ModeABC extends InputMode {
	private final ArrayList<ArrayList<String>> KEY_CHARACTERS = new ArrayList<>();

	private boolean shouldSelectNextLetter = false;

	// text analysis
	@NonNull private final AutoTextCase autoTextCase;
	@Nullable private final TextField textField;
	private final int textFieldTextCase;

	@Override public int getId() { return MODE_ABC; }


	protected ModeABC(@Nullable SettingsStore settings, @Nullable Language lang, @Nullable InputType inputType, @Nullable TextField textField) {
		super(settings, inputType);
		setLanguage(lang);
		defaultTextCase();

		autoTextCase = new AutoTextCase(settings, new Sequences(), inputType);
		this.textField = textField;
		textFieldTextCase = inputType == null ? CASE_UNDEFINED : inputType.determineTextCase();
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
			autoAcceptTimeout = settings.getAutoAcceptTimeoutAbc();
			shouldSelectNextLetter = true;
		} else {
			reset();
			autoAcceptTimeout = settings.getAutoAcceptTimeoutAbc();
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


	@Override
	public void determineNextWordTextCase(int nextDigit) {
		if (!settings.getAutoTextCaseAbc()) {
			return;
		}
		final String strBefore = textField != null ? textField.getStringBeforeCursor(10) : null;
		textCase = autoTextCase.determineNextLetterTextCase(textFieldTextCase, strBefore);
	}


	@Override
	public void skipNextTextCaseDetection() {
		autoTextCase.skipNext();
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
			if (settings.getAutoTextCaseAbc()) {
				allowedTextCases.add(CASE_CAPITALIZE);
			}
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

		final Text formattedModeString = new Text(language, modeString);
		return switch (textCase) {
			case CASE_UPPER -> formattedModeString.toUpperCase();
			case CASE_CAPITALIZE -> formattedModeString.capitalize();
			default -> formattedModeString.toLowerCase();
		};
	}
}
