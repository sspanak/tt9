package io.github.sspanak.tt9.ime.modes.helpers;

import java.util.regex.Pattern;

import io.github.sspanak.tt9.ime.helpers.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class AutoSpace {
	private final Pattern isLetter = Pattern.compile("^\\p{L}+$");
	private final Pattern isNumber = Pattern.compile("\\s*\\d+\\s*");
	private final Pattern isPunctuation = Pattern.compile("\\p{Punct}");

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
			&& !isNumber.matcher(previousChars).find()
			&& (
				shouldAddAutoSpaceAfterPunctuation(previousChars, nextKey)
				|| shouldAddAutoSpaceAfterWord(isWordAcceptedManually, nextChars)
			)
			&& !nextChars.startsWith(" ")
			&& !isPunctuation.matcher(nextChars).find();
	}


	/**
	 * shouldAddAutoSpaceAfterPunctuation
	 * Determines whether to automatically adding a space after certain punctuation signs makes sense.
	 * The rules are similar to the ones in the standard Android keyboard (with some exceptions,
	 * because we are not using a QWERTY keyboard here).
	 */
	private boolean shouldAddAutoSpaceAfterPunctuation(String previousChars, int nextKey) {
		return
			!previousChars.endsWith(" ") && !previousChars.endsWith("\n") && !previousChars.endsWith("\t")
			&& nextKey != 0 // no auto-space when the next composing character is whitespace or special
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
	 * shouldAddAutoSpaceAfterPunctuation
	 * Similar to "shouldAddAutoSpaceAfterPunctuation()", but determines whether to add a space after
	 * words.
	 */
	private boolean shouldAddAutoSpaceAfterWord(boolean isWordAcceptedManually, String nextChars) {
		return
			// Do not add space when auto-accepting words, because it feels very confusing when typing.
			isWordAcceptedManually
			// Secondary punctuation
			&& !lastSequence.equals("0")
			// Emoji
			&& !(lastSequence.startsWith("1") && lastSequence.length() > 1)
			// Right before another word
			&& !isLetter.matcher(nextChars).find();
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
