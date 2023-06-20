package io.github.sspanak.tt9.ime.modes.helpers;

import io.github.sspanak.tt9.TextTools;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class AutoTextCase {
	private final SettingsStore settings;


	public AutoTextCase(SettingsStore settingsStore) {
		settings = settingsStore;
	}

	/**
	 * adjustSuggestionTextCase
	 * In addition to uppercase/lowercase, here we use the result from determineNextWordTextCase(),
	 * to conveniently start sentences with capitals or whatnot.
	 *
	 * Also, by default we preserve any  mixed case words in the dictionary,
	 * for example: "dB", "Mb", proper names, German nouns, that always start with a capital,
	 * or Dutch words such as: "'s-Hertogenbosch".
	 */
	public String adjustSuggestionTextCase(Language language, String word, int newTextCase) {
		switch (newTextCase) {
			case InputMode.CASE_UPPER:
				return word.toUpperCase(language.getLocale());
			case InputMode.CASE_LOWER:
				return word.toLowerCase(language.getLocale());
			case InputMode.CASE_CAPITALIZE:
				return language.isMixedCaseWord(word) ? word : language.capitalize(word);
			case InputMode.CASE_DICTIONARY:
				return language.isMixedCaseWord(word) ? word : word.toLowerCase(language.getLocale());
			default:
				return word;
		}
	}


	/**
	 * determineNextWordTextCase
	 * Dynamically determine text case of words as the user types, to reduce key presses.
	 * For example, this function will return CASE_LOWER by default, but CASE_UPPER at the beginning
	 * of a sentence.
	 */
	public int determineNextWordTextCase(boolean isThereText, int currentTextCase, int textFieldTextCase, String textBeforeCursor) {
		if (
			// When the setting is off, don't do any changes.
			!settings.getAutoTextCase()
			// If the user wants to type in uppercase, this must be for a reason, so we better not override it.
			|| currentTextCase == InputMode.CASE_UPPER
		) {
			return currentTextCase;
		}


		if (textFieldTextCase != InputMode.CASE_UNDEFINED) {
			return textFieldTextCase;
		}

		// start of text
		if (!isThereText) {
			return InputMode.CASE_CAPITALIZE;
		}

		// start of sentence, excluding after "..."
		if (TextTools.isStartOfSentence(textBeforeCursor)) {
			return InputMode.CASE_CAPITALIZE;
		}

		if (TextTools.isNextToWord(textBeforeCursor)) {
			return InputMode.CASE_LOWER;
		}

		return InputMode.CASE_DICTIONARY;
	}
}
