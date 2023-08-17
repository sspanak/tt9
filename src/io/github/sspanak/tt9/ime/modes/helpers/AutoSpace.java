package io.github.sspanak.tt9.ime.modes.helpers;

import io.github.sspanak.tt9.TextTools;
import io.github.sspanak.tt9.ime.helpers.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class AutoSpace {
	private final SettingsStore settings;

	private InputType inputType;
	private TextField textField;
	private String lastWord;

	public AutoSpace(SettingsStore settingsStore) {
		settings = settingsStore;
	}

	public AutoSpace setInputType(InputType inputType) {
		this.inputType = inputType;
		return this;
	}

	public AutoSpace setTextField(TextField textField) {
		this.textField = textField;
		return this;
	}

	public AutoSpace setLastWord(String lastWord) {
		this.lastWord = lastWord;
		return this;
	}

	public boolean getAutoSpace() {
		return settings.getAutoSpace();
	}

	public AutoSpace setLastSequence() {
		return this;
	}

	/**
	 * shouldAddAutoSpace
	 * When the "auto-space" settings is enabled, this determines whether to automatically add a space
	 * at the end of a sentence or after accepting a suggestion. This allows faster typing, without
	 * pressing space.
	 *
	 * See the helper functions for the list of rules.
	 */
	public boolean shouldAddAutoSpace(int nextKey) {
		Character previousChar = textField.getPreviousChar();
		String nextChars = textField.getNextChars(2);

		return
			!inputType.isSpecialized()
			&& !TextTools.startsWithWhitespace(nextChars)
			&& (
				shouldAddAfterWord(previousChar, nextChars, nextKey)
				|| shouldAddAfterPunctuation(previousChar, nextChars, nextKey)
			);
	}


	/**
	 * shouldAddAfterPunctuation
	 * Determines whether to automatically adding a space after certain punctuation signs makes sense.
	 * The rules are similar to the ones in the standard Android keyboard (with some exceptions,
	 * because we are not using a QWERTY keyboard here).
	 */
	private boolean shouldAddAfterPunctuation(Character previousChar, String nextChars, int nextKey) {

		return
			nextKey != 1
			&& !TextTools.nextIsPunctuation(nextChars)
			&& !TextTools.startsWithNumber(nextChars)
			&& (
				previousChar == '.'
				|| previousChar == ','
				|| previousChar == ';'
				|| previousChar == ':'
				|| previousChar == '!'
				|| previousChar == '?'
				|| previousChar == ')'
				|| previousChar == ']'
				|| previousChar == '%'
				|| previousChar == '»'
				|| previousChar == '“'
			);
	}


	/**
	 * shouldAddAfterWord
	 * Similar to "shouldAddAfterPunctuation()", but determines whether to add a space after words.
	 */
	private boolean shouldAddAfterWord(Character previousChar, String nextChars, int nextKey) {
		return
			nextKey != 1
			&& nextChars.isEmpty()
			&& previousChar.isLetter(previousChar);
	}


	/**
	 * shouldDeletePrecedingSpace
	 * When the "auto-space" settings is enabled, determine whether to delete spaces before punctuation.
	 * This allows automatic conversion from: "words ." to: "words."
	 */
	public boolean shouldDeletePrecedingSpace() {
		return
			settings.getAutoSpace()
			&& (
				lastWord.equals(".")
				|| lastWord.equals(",")
				|| lastWord.equals(";")
				|| lastWord.equals(":")
				|| lastWord.equals("!")
				|| lastWord.equals("?")
				|| lastWord.equals(")")
				|| lastWord.equals("]")
				|| lastWord.equals("'")
				|| lastWord.equals("@")
			)
			&& !inputType.isSpecialized();
	}
}
