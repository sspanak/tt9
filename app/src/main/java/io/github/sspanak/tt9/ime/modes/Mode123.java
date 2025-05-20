package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

class Mode123 extends ModePassthrough {
	@Override public int getId() { return MODE_123; }
	@Override @NonNull public String toString() { return "123"; }

	@Override public int getSequenceLength() { return digitSequence.length(); }
	@Override public boolean shouldAcceptPreviousSuggestion(String currentWord, int nextKey, boolean hold) { return true; }

	private final ArrayList<ArrayList<String>> KEY_CHARACTERS = new ArrayList<>();


	protected Mode123(SettingsStore settings, Language language, InputType inputType) {
		super(settings, inputType);
		setLanguage(language);

		if (inputType.isPhoneNumber()) {
			setSpecificSpecialCharacters(Characters.Phone, false);
		} else if (inputType.isNumeric()) {
			setSpecificSpecialCharacters(Characters.getAllForDecimal(inputType.isDecimal(), inputType.isSignedNumber()), false);
		} else if (isEmailMode) {
			setSpecificSpecialCharacters(Characters.Email, true);
		} else {
			setDefaultSpecialCharacters();
		}
	}


	private void setSpecificSpecialCharacters(ArrayList<ArrayList<String>> chars, boolean sort) {
		for (int group = 0; group < chars.size(); group++) {
			KEY_CHARACTERS.add(
				sort ? Characters.orderByList(chars.get(group), settings.getOrderedKeyChars(language, group), isEmailMode) : new ArrayList<>(chars.get(group))
			);
		}
	}


	/**
	 * setDefaultSpecialCharacters
	 * Special characters for when the user has selected 123 mode in a text field. In this case, we just
	 * use the default list, but reorder it a bit for convenience. We enforce English characters, to
	 * ensure number field compatibility with all apps and websites.
	 */
	private void setDefaultSpecialCharacters() {
		Language english = LanguageCollection.getByLocale("en");
		KEY_CHARACTERS.add(
			TextTools.removeLettersFromList(orderCharsForNumericField(settings.getOrderedKeyChars(english, 0)))
		);
		KEY_CHARACTERS.add(
			TextTools.removeLettersFromList(orderCharsForNumericField(settings.getOrderedKeyChars(english, 1)))
		);
	}


	private ArrayList<String> orderCharsForNumericField(@NonNull ArrayList<String> unordered) {
		ArrayList<String> ordered = new ArrayList<>();

		if (unordered.contains(".")) {
			ordered.add(".");
		}

		if (unordered.contains("+")) {
			ordered.add("+");
		}

		for (String character : unordered) {
			if (!character.equals("+") && !character.equals(".") && !character.equals("\n")) {
				ordered.add(character);
			}
		}

		return ordered;
	}


	@Override public boolean onBackspace() {
		if (suggestions.isEmpty()) {
			return false;
		}

		reset();
		return true;
	}


	@Override public boolean onNumber(int number, boolean hold, int repeat) {
		reset();
		digitSequence = String.valueOf(number);

		if (hold && number < KEY_CHARACTERS.size() && !KEY_CHARACTERS.get(number).isEmpty()) {
			suggestions.addAll(KEY_CHARACTERS.get(number));
		} else {
			autoAcceptTimeout = 0;
			suggestions.add(digitSequence);
		}

		return true;
	}


	/**
	 * shouldIgnoreText
	 * Since this is a numeric mode, we allow typing only numbers and:
	 * 	1. TAB
	 * 	2. Math chars for numeric fields
	 * 	3. Various punctuation chars for dialer fields, because they are used as dialing shortcuts
	 * 	at least in Japan. More info and discussion: <a href="https://github.com/sspanak/tt9/issues/241">issue 241 on Github</a>.
	 */
	@Override public boolean shouldIgnoreText(String text) {
		return
			text == null
			|| text.length() != 1
			|| text.charAt(0) == 9
			|| !(
				(text.charAt(0) > 31 && text.charAt(0) < 65)
				|| (text.charAt(0) > 90 && text.charAt(0) < 97)
				|| (text.charAt(0) > 122 && text.charAt(0) < 127)
			);
	}


	@Override public void onAcceptSuggestion(@NonNull String ignored, boolean ignored2) {
		reset();
	}


	@Override public void reset() {
		super.reset();
		digitSequence = "";
	}
}
