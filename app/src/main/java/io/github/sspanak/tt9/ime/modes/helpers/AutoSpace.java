package io.github.sspanak.tt9.ime.modes.helpers;

import io.github.sspanak.tt9.languages.Text;
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
	public boolean shouldAddAutoSpace(boolean isWordAcceptedManually, int nextKey) {
		String previousChars = textField.getStringBeforeCursor(2);
		Text nextChars = textField.getTextAfterCursor(2);

		return
			settings.getAutoSpace()
			&& !inputType.isSpecialized()
			&& nextKey != 0
			&& !nextChars.startsWithWhitespace()
			&& (
				shouldAddAfterWord(isWordAcceptedManually, previousChars, nextChars, nextKey)
				|| shouldAddAfterPunctuation(previousChars, nextChars, nextKey)
			);
	}


	/**
	 * shouldAddAfterPunctuation
	 * Determines whether to automatically adding a space after certain punctuation signs makes sense.
	 * The rules are similar to the ones in the standard Android keyboard (with some exceptions,
	 * because we are not using a QWERTY keyboard here).
	 */
	private boolean shouldAddAfterPunctuation(String previousChars, Text nextChars, int nextKey) {
		char previousChar = previousChars.isEmpty() ? 0 : previousChars.charAt(previousChars.length() - 1);

		return
			nextKey != 1
			&& !nextChars.nextIsPunctuation()
			&& !nextChars.startsWithNumber()
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
				|| previousChar == '؟'
				|| previousChar == '“'
				|| previousChars.endsWith(" -")
				|| previousChars.endsWith(" /")
			);
	}


	/**
	 * shouldAddAfterWord
	 * Similar to "shouldAddAfterPunctuation()", but determines whether to add a space after words.
	 */
	private boolean shouldAddAfterWord(boolean isWordAcceptedManually, String previousChars, Text nextChars, int nextKey) {
		return
			isWordAcceptedManually // Do not add space when auto-accepting words, because it feels very confusing when typing.
			&& nextKey != 1
			&& nextChars.isEmpty()
			&& Text.previousIsLetter(previousChars);
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
				|| lastWord.equals("؟")
				|| lastWord.equals(")")
				|| lastWord.equals("]")
				|| lastWord.equals("'")
				|| lastWord.equals("@")
			)
			&& !inputType.isSpecialized();
	}
}
