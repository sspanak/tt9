package io.github.sspanak.tt9.ime.modes.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;

public class AutoTextCase {
	@NonNull private final Sequences sequences;
	@NonNull private final SettingsStore settings;
	private final boolean isUs;
	private boolean skipNext;


	public AutoTextCase(@NonNull SettingsStore settingsStore, @NonNull Sequences sequences, @Nullable InputType inputType) {
		this.sequences = sequences;
		settings = settingsStore;
		isUs = inputType != null && inputType.isUs();
		skipNext = false;
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
	public int determineNextWordTextCase(Language language, int currentTextCase, int textFieldTextCase, String beforeCursor, String digitSequence) {
		if (
			// When the setting is off, don't do any changes.
			!settings.getAutoTextCase()
			// If the user has explicitly selected uppercase, we respect that.
			|| currentTextCase == InputMode.CASE_UPPER
			// we do not have text fields that expect sentences, so disable the feature to save some resources
			|| isUs
		) {
			return currentTextCase;
		}

		if (skipNext) {
			skipNext = false;
			return textFieldTextCase != InputMode.CASE_UNDEFINED ? textFieldTextCase : currentTextCase;
		}

		// lowercase also takes priority but not as strict as uppercase
		if (textFieldTextCase != InputMode.CASE_UNDEFINED && currentTextCase != InputMode.CASE_LOWER) {
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

		// 1. Stay in lowercase within the same sentence, in case the user has selected lowercase.
		// or 2. Prevent English "I", inserted in the middle of a word, from being uppercase.
		if (currentTextCase == InputMode.CASE_LOWER || (sequences.isEnglishI(language, digitSequence) && Text.isNextToWord(beforeCursor))) {
			return InputMode.CASE_LOWER;
		}

		return InputMode.CASE_DICTIONARY;
	}


	public void skipNext() {
		skipNext = true;
	}

	public void doNotSkipNext() {
		skipNext = false;
	}
}
