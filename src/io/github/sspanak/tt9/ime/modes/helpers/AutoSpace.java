package io.github.sspanak.tt9.ime.modes.helpers;

import java.util.regex.Pattern;

import io.github.sspanak.tt9.ime.helpers.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class AutoSpace {
	private final Pattern isNumber = Pattern.compile("\\s*\\d+\\s*");
	private final Pattern nextIsLetter = Pattern.compile("^\\p{L}+");
	private final Pattern nextIsPunctuation = Pattern.compile("^\\p{Punct}");

	private final SettingsStore settings;

	private InputType inputType;
	private TextField textField;
	private String lastWord;
	private String lastSequence;

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

	public AutoSpace setLastSequence(String lastSequence) {
		this.lastSequence = lastSequence;
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
		String previousChars = textField.getPreviousChars(2);
		String nextChars = textField.getNextChars(2);

		return
			settings.getAutoSpace()
			&& !inputType.isSpecialized()
			&& !nextChars.startsWith(" ")
			&& !isNumber.matcher(previousChars).find()
			&& !nextIsPunctuation.matcher(nextChars).find()
			&& (
				shouldAddAfterPunctuation(previousChars, nextKey)
				|| shouldAddAfterWord(isWordAcceptedManually, nextChars)
			);
	}


	/**
	 * shouldAddAfterPunctuation
	 * Determines whether to automatically adding a space after certain punctuation signs makes sense.
	 * The rules are similar to the ones in the standard Android keyboard (with some exceptions,
	 * because we are not using a QWERTY keyboard here).
	 */
	private boolean shouldAddAfterPunctuation(String previousChars, int nextKey) {
		return
			// no space after whitespace or special characters
			!previousChars.endsWith(" ") && !previousChars.endsWith("\n") && !previousChars.endsWith("\t") // previous whitespace
			&& !lastSequence.equals("0") // previous previous math/special char
			&& nextKey != 0 // composing (upcoming) whitespace or special character

			// add space after the these
			&& (
				previousChars.endsWith(".")
				|| previousChars.endsWith(",")
				|| previousChars.endsWith(";")
				|| previousChars.endsWith(":")
				|| previousChars.endsWith("!")
				|| previousChars.endsWith("?")
				|| previousChars.endsWith(")")
				|| previousChars.endsWith("]")
				|| previousChars.endsWith("%")
				|| previousChars.endsWith(" -")
				|| previousChars.endsWith(" /")
			);
	}


	/**
	 * shouldAddAfterPunctuation
	 * Similar to "shouldAddAfterPunctuation()", but determines whether to add a space after words.
	 */
	private boolean shouldAddAfterWord(boolean isWordAcceptedManually, String nextChars) {
		return
			// Do not add space when auto-accepting words, because it feels very confusing when typing.
			isWordAcceptedManually
			// Right before another word
			&& !nextIsLetter.matcher(nextChars).find();
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
