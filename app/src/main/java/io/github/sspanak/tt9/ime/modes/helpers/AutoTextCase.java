package io.github.sspanak.tt9.ime.modes.helpers;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;

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
		return switch (newTextCase) {
			case InputMode.CASE_UPPER -> word.toUpperCase();
			case InputMode.CASE_LOWER -> word.toLowerCase();
			case InputMode.CASE_CAPITALIZE ->
				word.isMixedCase() || word.isUpperCase() ? word.toString() : word.capitalize();
			default -> word.toString();
		};
	}


	/**
	 * determineNextWordTextCase
	 * Dynamically determine text case of words as the user types, to reduce key presses.
	 * For example, this function will return CASE_LOWER by default, but CASE_UPPER at the beginning
	 * of a sentence.
	 */
	public int determineNextWordTextCase(int currentTextCase, int textFieldTextCase, String beforeCursor, String digitSequence) {
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
		if (
			beforeCursor != null
			&& (
				beforeCursor.isEmpty()
				|| (settings.getAutoCapitalsAfterNewline() && beforeCursor.endsWith("\n"))
			)
		) {
			return InputMode.CASE_CAPITALIZE;
		}

		// start of sentence, excluding after "..."
		if (Text.isStartOfSentence(beforeCursor)) {
			return InputMode.CASE_CAPITALIZE;
		}

		// This is mostly for English "I", inserted in the middle of a word. However, we don't want to
		// enforce lowercase for words like "-ROM" in "CD-ROM". We have to use the digitSequence here,
		// because the composing text is not yet set in some cases, when this is called.
		if (Text.isNextToWord(beforeCursor) && !digitSequence.startsWith("1")) {
			return InputMode.CASE_LOWER;
		}

		return InputMode.CASE_DICTIONARY;
	}
}
