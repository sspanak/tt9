package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.util.Characters;

public class Mode123 extends ModePassthrough {
	@Override public int getId() { return MODE_123; }
	@Override @NonNull public String toString() { return "123"; }

	@Override public final boolean is123() { return true; }
	@Override public boolean isPassthrough() { return false; }
	@Override public int getSequenceLength() { return digitSequence.length(); }
	@Override public boolean shouldAcceptPreviousSuggestion(int nextKey) { return true; }

	private final ArrayList<ArrayList<String>> KEY_CHARACTERS = new ArrayList<>();
	private final boolean isEmailMode;


	public Mode123(InputType inputType, Language language) {
		this.language = language;
		isEmailMode = inputType.isEmail();

		if (inputType.isPhoneNumber()) {
			setSpecificSpecialCharacters(Characters.Phone);
		} else if (inputType.isNumeric()) {
			setSpecificSpecialCharacters(Characters.getNumberSpecialCharacters(inputType.isDecimal(), inputType.isSignedNumber()));
		} else if (inputType.isEmail()) {
			setSpecificSpecialCharacters(Characters.Email);
		} else {
			setDefaultSpecialCharacters();
		}

	}


	private void setSpecificSpecialCharacters(ArrayList<ArrayList<String>> chars) {
		for (ArrayList<String> group : chars) {
			KEY_CHARACTERS.add(new ArrayList<>(group));
		}
	}


	/**
	 * setDefaultSpecialCharacters
	 * Special characters for when the user has selected 123 mode in a text field. In this case, we just
	 * use the default list, but reorder it a bit for convenience.
	 */
	private void setDefaultSpecialCharacters() {
		// 0-key
		KEY_CHARACTERS.add(new ArrayList<>(Collections.singletonList("+")));
		for (String character : Characters.Special) {
			if (!character.equals("+") && !character.equals("\n")) {
				KEY_CHARACTERS.get(0).add(character);
			}
		}

		// 1-key
		KEY_CHARACTERS.add(new ArrayList<>(Collections.singletonList(".")));
		for (String character : Characters.PunctuationEnglish) {
			if (!character.equals(".")) {
				KEY_CHARACTERS.get(1).add(character);
			}
		}
	}


	@Override protected boolean nextSpecialCharacters() {
		return !isEmailMode && digitSequence.equals(NaturalLanguage.SPECIAL_CHARS_KEY) && super.nextSpecialCharacters();
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

	@Override public void onAcceptSuggestion(TextField textField, @NonNull String ignored, boolean ignored2) {
		reset();
	}

	@Override public void reset() {
		super.reset();
		digitSequence = "";
	}
}
