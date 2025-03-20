package io.github.sspanak.tt9.ime.modes.helpers;

import java.util.Set;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NullLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.chars.Characters;

public class AutoSpace {
	private static final Set<Character> PRECEDING_SPACE_PUNCTUATION = Set.of('(', '«', '„');
	private static final Set<Character> PRECEDING_SPACE_FRENCH_PUNCTUATION = Set.of(';', ':', '!', '?', '»');
	private static final Set<Character> TRAILING_SPACE_PUNCTUATION = Set.of(';', '!', '?', ')', '%', '»', '؟', '“', Characters.GR_QUESTION_MARK.charAt(0));

	private static final Set<Character> NO_PRECEDING_SPACE_PUNCTUATION = Set.of('.', ',', ')', '\'', '@', '“', '؟', Characters.GR_QUESTION_MARK.charAt(0));
	private static final Set<Character> NOT_FRENCH_NO_PRECEDING_SPACE_PUNCTUATION = Set.of(';', ':', '!', '?', '»');

	private Language language;
	private final SettingsStore settings;

	private boolean isLanguageFrench;
	private boolean isLanguageWithAlphabet;
	private boolean isLanguageWithSpaceBetweenWords;


	public AutoSpace(SettingsStore settingsStore) {
		language = new NullLanguage();
		settings = settingsStore;
		isLanguageWithAlphabet = false;
		isLanguageFrench = false;
		isLanguageWithSpaceBetweenWords = true;
	}


	public AutoSpace setLanguage(Language lang) {
		language = language == null ? new NullLanguage() : lang;
		isLanguageFrench = LanguageKind.isFrench(lang);
		isLanguageWithAlphabet = !language.isSyllabary();
		isLanguageWithSpaceBetweenWords = language.hasSpaceBetweenWords();
		return this;
	}


	/**
	 * Determines whether to automatically add a space at the end of a sentence or after accepting a
	 * suggestion. This allows faster typing, without pressing space. See the helper functions for
	 * the list of rules.
	 */
	public boolean shouldAddTrailingSpace(TextField textField, InputType inputType, boolean isWordAcceptedManually, int nextKey) {
		if (
			!isLanguageWithSpaceBetweenWords
			|| nextKey == 0
			|| !settings.getAutoSpace()
			|| inputType.isSpecialized()
			|| inputType.isUs()
		) {
			return false;
		}

		String previousChars = textField.getStringBeforeCursor(2);
		Text nextChars = textField.getTextAfterCursor(2);

		return
			!nextChars.startsWithWhitespace()
			&& (
				shouldAddAfterWord(isWordAcceptedManually, previousChars, nextChars, nextKey)
				|| shouldAddAfterPunctuation(previousChars, nextChars, nextKey)
			);
	}


	/**
	 * Determines the special French rules for space before punctuation, as well as some standard ones.
	 * For example, should we transform "word?" to "word ?", or "something(" to "something ("
	 */
	public boolean shouldAddBeforePunctuation(InputType inputType, TextField textField) {
		if (
			!isLanguageWithSpaceBetweenWords
			|| !settings.getAutoSpace()
			|| inputType.isSpecialized()
			|| inputType.isUs()
		) {
			return false;
		}

		String previousChars = textField.getStringBeforeCursor(2);
		char penultimateChar = previousChars.length() < 2 ? 0 : previousChars.charAt(previousChars.length() - 2);
		char previousChar = previousChars.isEmpty() ? 0 : previousChars.charAt(previousChars.length() - 1);

		if (previousChar == '¡' || previousChar == '¿' && settings.getAutoSpace()) {
			return true;
		}

		return
			Character.isAlphabetic(penultimateChar)
			&& (
				PRECEDING_SPACE_PUNCTUATION.contains(previousChar)
				|| (isLanguageFrench && PRECEDING_SPACE_FRENCH_PUNCTUATION.contains(previousChar))
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
				TRAILING_SPACE_PUNCTUATION.contains(previousChar)
				|| (isLanguageFrench && previousChar == '«')
				|| (penultimateChar == ' ' && previousChar == '-')
				|| (penultimateChar == ' ' && previousChar == '/')
				|| (!Character.isDigit(penultimateChar) && previousChar == ':')
				|| (!Character.isDigit(penultimateChar) && previousChar == '.')
				|| (!Character.isDigit(penultimateChar) && previousChar == ',')
				|| (
					Character.isDigit(penultimateChar) && Characters.isCurrency(language, String.valueOf(previousChar))
				)
			);
	}


	/**
	 * Similar to "shouldAddAfterPunctuation()", but determines whether to add a space after words.
	 */
	private boolean shouldAddAfterWord(boolean isWordAcceptedManually, String previousChars, Text nextChars, int nextKey) {
		return
			isWordAcceptedManually // Do not add space when auto-accepting words, because it feels very confusing when typing.
			&& isLanguageWithAlphabet
			&& nextKey != 1
			&& nextChars.isEmpty()
			&& Text.previousIsLetter(previousChars);
	}


	/**
	 * Determines whether to transform: "word ." to: "word."
	 */
	public boolean shouldDeletePrecedingSpace(InputType inputType, TextField textField) {
		if (
			!isLanguageWithSpaceBetweenWords
			|| !settings.getAutoSpace()
			|| inputType.isSpecialized()
			|| inputType.isUs()
		) {
			return false;
		}


		String previousChars = textField.getStringBeforeCursor(3);
		char prePenultimateChar = previousChars.length() < 3 ? 0 : previousChars.charAt(previousChars.length() - 3);
		char penultimateChar = previousChars.length() < 2 ? 0 : previousChars.charAt(previousChars.length() - 2);
		char previousChar = previousChars.isEmpty() ? 0 : previousChars.charAt(previousChars.length() - 1);

		return
			!Character.isWhitespace(prePenultimateChar)
			&& Character.isWhitespace(penultimateChar)
			&& (
				NO_PRECEDING_SPACE_PUNCTUATION.contains(previousChar)
				|| (!isLanguageFrench && NOT_FRENCH_NO_PRECEDING_SPACE_PUNCTUATION.contains(previousChar))
			);
	}
}
