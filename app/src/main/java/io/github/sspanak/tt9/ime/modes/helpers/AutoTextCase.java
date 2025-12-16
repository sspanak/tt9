package io.github.sspanak.tt9.ime.modes.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;

public class AutoTextCase {
	@NonNull private final Sequences sequences;
	@NonNull private final SettingsStore settings;
	private final boolean isSpecialized;
	private boolean skipNext;


	public AutoTextCase(@NonNull SettingsStore settingsStore, @NonNull Sequences sequences, @Nullable InputType inputType) {
		this.sequences = sequences;
		settings = settingsStore;
		isSpecialized = inputType != null && (inputType.isSpecialized() || inputType.isUs());
		skipNext = false;
	}


	/**
	 * Changes the text case of a word. Usually, used together with determineNextWordTextCase(), which
	 * would inspect the current text field, the app Shift state, whether we are at the beginning of a
	 * sentence, and other factors to determine the correct text case for the next word.
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
	 * Uses determineNextWordTextCase() and adjustSuggestionTextCase() to adjust the text case
	 * of all words in a paragraph.	Useful for voice input results.
	 */
	public String adjustParagraphTextCase(@NonNull Language language, @Nullable String paragraph, @NonNull String textBeforeSpeech, int inputModeTextCase, int textFieldTextCase) {
		if (paragraph == null || paragraph.isEmpty()) {
			return paragraph;
		}

		final String dummySequence = "";
		final StringBuilder output = new StringBuilder(paragraph.length());

		for (String word : paragraph.split(" ")) {
			final int textCase = determineNextWordTextCase(language, inputModeTextCase, textFieldTextCase, null, dummySequence, textBeforeSpeech + output);
			final String adjusted = adjustSuggestionTextCase(new Text(language, word), textCase);
			output.append(adjusted).append(" ");
		}

		return output.toString();
	}


	/**
	 * The analog of determineNextWordTextCase() for modes like ABC, where letters are input one by
	 * one. We use very similar, but not exactly the same logic, due to the lack of real words, and
	 * dictionary context.
	 */
	public int determineNextLetterTextCase(@NonNull Language language, int textFieldTextCase, @Nullable String beforeCursor) {
		final int settingsTextCase = settings.getTextCase();

		if (isSpecialized || settingsTextCase == InputMode.CASE_UPPER || !language.hasUpperCase()) {
			return settingsTextCase;
		}

		if (skipNext) {
			skipNext = false;
			return settingsTextCase;
		}

		// lowercase also takes priority but not as strict as uppercase
		if (textFieldTextCase != InputMode.CASE_UNDEFINED && settingsTextCase != InputMode.CASE_LOWER) {
			return textFieldTextCase;
		}

		// start of text or sentence
		if (textFieldTextCase == InputMode.CASE_UPPER || beforeCursor == null || beforeCursor.isEmpty() || Text.isStartOfSentence(beforeCursor)) {
			return InputMode.CASE_UPPER;
		}

		// beginning of a new word in a text field that requires capitalization
		if (textFieldTextCase == InputMode.CASE_CAPITALIZE && !Text.isNextToWord(beforeCursor)) {
			return InputMode.CASE_UPPER;
		}

		return InputMode.CASE_LOWER;
	}


	/**
	 * determineNextWordTextCase
	 * Dynamically determine text case of words as the user types, to reduce key presses.
	 * For example, this function will return CASE_LOWER by default, but CASE_UPPER at the beginning
	 * of a sentence.
	 */
	public int determineNextWordTextCase(@NonNull Language language, int currentTextCase, int textFieldTextCase, @Nullable TextField textField, @Nullable String digitSequence, @Nullable String beforeCursor) {
		if (
			// When the setting is off or invalid, don't do any changes.
			!settings.getAutoTextCasePredictive()
			// If the user has explicitly selected uppercase, we respect that.
			|| currentTextCase == InputMode.CASE_UPPER
			// preserve the text case in special input fields, like email, urls, passwords, or our own
			|| isSpecialized
			// save resources if the language has no uppercase letters
			|| !language.hasUpperCase()
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
		String before = beforeCursor == null && textField != null ? textField.getStringBeforeCursor() : beforeCursor;
		if (before == null || before.isEmpty() || (settings.getAutoCapitalsAfterNewline() && before.endsWith("\n"))) {
			return InputMode.CASE_CAPITALIZE;
		}

		// start of sentence, excluding after "..."
		if (Text.isStartOfSentence(before)) {
			return InputMode.CASE_CAPITALIZE;
		}

		// 1. Stay in lowercase within the same sentence, in case the user has selected lowercase.
		// or 2. Prevent English "I", inserted in the middle of a word, from being uppercase.
		if (currentTextCase == InputMode.CASE_LOWER || (sequences.isEnglishI(language, digitSequence) && Text.isNextToWord(before))) {
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
