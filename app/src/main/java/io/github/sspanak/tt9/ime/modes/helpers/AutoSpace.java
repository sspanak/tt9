package io.github.sspanak.tt9.ime.modes.helpers;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Characters;
import io.github.sspanak.tt9.util.Text;

public class AutoSpace {
	private final SettingsStore settings;

	private InputType inputType;
	private TextField textField;
	private String lastWord;

	private boolean isLanguageFrench;
	private boolean isLanguageWithSpaceBetweenWords;


	public AutoSpace(SettingsStore settingsStore) {
		settings = settingsStore;
		isLanguageFrench = false;
		isLanguageWithSpaceBetweenWords = true;
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


	public AutoSpace setLanguage(Language language) {
		isLanguageFrench = LanguageKind.isFrench(language);
		isLanguageWithSpaceBetweenWords = language != null && language.hasSpaceBetweenWords();
		return this;
	}

	public AutoSpace setLastSequence() {
		return this;
	}


	/**
	 * Determines whether to automatically add a space at the end of a sentence or after accepting a
	 * suggestion. This allows faster typing, without pressing space. See the helper functions for
	 * the list of rules.
	 */
	public boolean shouldAddTrailingSpace(boolean isWordAcceptedManually, int nextKey) {
		if (!isLanguageWithSpaceBetweenWords) {
			return false;
		}

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
	 * Determines the special French rules for space before punctuation, as well as some standard ones.
	 * For example, should we transform "word?" to "word ?", or "something(" to "something ("
	 */
	public boolean shouldAddBeforePunctuation() {
		String previousChars = textField.getStringBeforeCursor(2);
		char penultimateChar = previousChars.length() < 2 ? 0 : previousChars.charAt(previousChars.length() - 2);
		char previousChar = previousChars.isEmpty() ? 0 : previousChars.charAt(previousChars.length() - 1);

		if (previousChar == '¡'	|| previousChar == '¿' && settings.getAutoSpace()) {
			return true;
		}

		return
			isLanguageWithSpaceBetweenWords
			&& settings.getAutoSpace()
			&& !inputType.isSpecialized()
			&& Character.isAlphabetic(penultimateChar)
			&& (
				previousChar == '('
				|| previousChar == '['
				|| previousChar == '«'
				|| previousChar == '„'
				|| isLanguageFrench && (
					previousChar == ';'
					|| previousChar == ':'
					|| previousChar == '!'
					|| previousChar == '?'
					|| previousChar == ')'
					|| previousChar == '»'
				)
			);
	}


	/**
	 * Determines whether to automatically adding a space after certain punctuation signs makes sense.
	 * The rules are similar to the ones in the standard Android keyboard (with some exceptions,
	 * because we are not using a QWERTY keyboard here).
	 */
	private boolean shouldAddAfterPunctuation(String previousChars, Text nextChars, int nextKey) {
		char penultimateChar = previousChars.length() < 2 ? 0 : previousChars.charAt(previousChars.length() - 2);
		char previousChar = previousChars.isEmpty() ? 0 : previousChars.charAt(previousChars.length() - 1);

		return
			nextKey != 1
			&& !Text.nextIsPunctuation(nextChars.toString())
			&& !nextChars.startsWithNumber()
			&& (
				previousChar == '.'
				|| previousChar == ','
				|| previousChar == ';'
				|| (previousChar == ':' && !Character.isDigit(penultimateChar))
				|| previousChar == '!'
				|| previousChar == '?'
				|| previousChar == ')'
				|| previousChar == ']'
				|| previousChar == '%'
				|| previousChar == '»'
				|| previousChar == '؟'
				|| previousChar == '“'
				|| (isLanguageFrench && previousChar == '«')
				|| (penultimateChar == ' ' && previousChar == '-')
				|| (penultimateChar == ' ' && previousChar == '/')
				|| (Character.isDigit(penultimateChar) && Characters.Currency.contains(previousChar + ""))
			);
	}


	/**
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
	 * Determines whether to transform: "word ." to: "word."
	 */
	public boolean shouldDeletePrecedingSpace() {
		return
			isLanguageWithSpaceBetweenWords
			&& settings.getAutoSpace()
			&& (
				lastWord.equals(".")
				|| lastWord.equals(",")
				|| (!isLanguageFrench && lastWord.equals(";"))
				|| (!isLanguageFrench && lastWord.equals(":"))
				|| (!isLanguageFrench && lastWord.equals("!"))
				|| (!isLanguageFrench && lastWord.equals("?"))
				|| lastWord.equals("؟")
				|| (!isLanguageFrench && lastWord.equals(")"))
				|| lastWord.equals("]")
				|| lastWord.equals("'")
				|| lastWord.equals("@")
			)
			&& !inputType.isSpecialized();
	}
}
