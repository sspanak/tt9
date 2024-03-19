package io.github.sspanak.tt9.ime.modes.helpers;

import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.ime.modes.InputMode;
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
	 * Also, by default we preserve any mixed case words in the dictionary,
	 * for example: "dB", "Mb", proper names, German nouns, that always start with a capital,
	 * or Dutch words such as: "'s-Hertogenbosch".
	 */
	public String adjustSuggestionTextCase(Text word, int newTextCase) {
		switch (newTextCase) {
			case InputMode.CASE_UPPER:
				return word.toUpperCase();
			case InputMode.CASE_LOWER:
				return word.toLowerCase();
			case InputMode.CASE_CAPITALIZE:
				return word.isMixedCase() || word.isUpperCase() ? word.toString() : word.capitalize();
			default:
				return word.toString();
		}
	}


	/**
	 * determineNextWordTextCase
	 * Dynamically determine text case of words as the user types, to reduce key presses.
	 * For example, this function will return CASE_LOWER by default, but CASE_UPPER at the beginning
	 * of a sentence.
	 */
	public int determineNextWordTextCase(int currentTextCase, int textFieldTextCase, String textBeforeCursor) {
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
		if (textBeforeCursor != null && textBeforeCursor.isEmpty()) {
			return InputMode.CASE_CAPITALIZE;
		}

		// start of sentence, excluding after "..."
		if (Text.isStartOfSentence(textBeforeCursor)) {
			return InputMode.CASE_CAPITALIZE;
		}

		// this is mostly for English "I"
		if (Text.isNextToWord(textBeforeCursor)) {
			return InputMode.CASE_LOWER;
		}

		return InputMode.CASE_DICTIONARY;
	}
}
