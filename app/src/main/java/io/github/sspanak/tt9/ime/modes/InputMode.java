package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.Sequences;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NullLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.tray.SuggestionsBar;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.chars.Characters;

abstract public class InputMode {
	// typing mode
	public static final int MODE_PREDICTIVE = 0;
	public static final int MODE_ABC = 1;
	public static final int MODE_123 = 2;
	public static final int MODE_PASSTHROUGH = 4;
	public static final int MODE_HIRAGANA = 5;
	public static final int MODE_KATAKANA = 6;
	public static final int MODE_RECOMPOSING = 7;

	// text case
	public static final int CASE_UNDEFINED = -1;
	public static final int CASE_UPPER = 0;
	public static final int CASE_CAPITALIZE = 1;
	public static final int CASE_LOWER = 2;
	public static final int CASE_DICTIONARY = 3; // do not force it, but use the dictionary word as-is
	protected final ArrayList<Integer> allowedTextCases = new ArrayList<>();
	protected int textCase = CASE_LOWER;

	// data
	protected int autoAcceptTimeout = -1;
	@NonNull protected String digitSequence = "";
	protected final boolean isEmailMode;
	@NonNull protected Language language = new NullLanguage();
	protected final SettingsStore settings;
	@NonNull protected final ArrayList<String> suggestions = new ArrayList<>();
	@NonNull protected Runnable onSuggestionsUpdated = () -> {};
	@NonNull protected Sequences seq = new Sequences();


	protected InputMode(SettingsStore settings, InputType inputType) {
		allowedTextCases.add(CASE_LOWER);
		isEmailMode = inputType != null && inputType.isEmail() && !inputType.isDefectiveText();
		this.settings = settings;
	}


	public static InputMode getInstance(@Nullable SettingsStore settings, @Nullable Language language, @Nullable InputType inputType, @Nullable TextField textField, int mode) {
		if (mode != MODE_PASSTHROUGH && (settings == null || language == null)) {
			mode = MODE_PASSTHROUGH;
			Logger.w(InputMode.class.getSimpleName(), "Cannot create a new InputMode without Settings and Language. Defaulting to MODE_PASSTHROUGH.");
		}

		switch (mode) {
			case MODE_PREDICTIVE:
				if (LanguageKind.isChineseBopomofo(language)) return new ModeBopomofo(settings, language, inputType, textField);
				if (LanguageKind.isChinesePinyin(language)) return new ModePinyin(settings, language, inputType, textField);
				if (LanguageKind.isJapanese(language)) return new ModeKanji(settings, language, inputType, textField);
				if (LanguageKind.isKorean(language)) return new ModeCheonjiin(settings, inputType, textField);
				if (language.isTranscribed()) return new ModeIdeograms(settings, language, inputType, textField);
				return new ModeWords(settings, language, inputType, textField);
			case MODE_HIRAGANA:
				if (LanguageKind.isJapanese(language)) return new ModeHiragana(settings, language, inputType, textField);
				return new ModeABC(settings, language, inputType);
			case MODE_KATAKANA:
				if (LanguageKind.isJapanese(language)) return new ModeKatakana(settings, language, inputType, textField);
				return new ModeABC(settings, language, inputType);
			case MODE_ABC:
				return new ModeABC(settings, language, inputType);
			case MODE_RECOMPOSING:
				return new ModeRecomposing(settings, language, inputType, textField);
			case MODE_PASSTHROUGH:
				return new ModePassthrough(settings, inputType);
			default:
				Logger.w(InputMode.class.getSimpleName(), "Defaulting to mode: " + Mode123.class.getName() + " for unknown InputMode: " + mode);
			case MODE_123:
				return new Mode123(settings, language, inputType);
		}
	}


	public InputMode copy(InputMode other) {
		if (other == null || InputModeKind.isNumeric(this) || InputModeKind.isNumeric(other)) {
			return this;
		}

		autoAcceptTimeout = -1;
		textCase = other.textCase;
		setSequence(other.digitSequence);

		return this;
	}


	// Key handlers. Return "true" when handling the key or "false", when is nothing to do.
	public boolean onBackspace() { return false; }
	abstract public boolean onNumber(int number, boolean hold, int repeat, @NonNull String[] surroundingChars);

	// Suggestions
	public void onAcceptSuggestion(@NonNull String word) { onAcceptSuggestion(word, false); }
	public void onAcceptSuggestion(@NonNull String word, boolean preserveWordList) {}
	public void onCursorMove(@NonNull String word) { if (!digitSequence.isEmpty()) onAcceptSuggestion(word); }
	public boolean onReplaceSuggestion(@NonNull String rawWord) {
		String newSequence;
		if (SuggestionsBar.SHOW_GROUP_0_SUGGESTION.equalsIgnoreCase(rawWord)) {
			newSequence = seq.CHARS_GROUP_0_SEQUENCE;
		} else if (SuggestionsBar.SHOW_GROUP_1_SUGGESTION.equalsIgnoreCase(rawWord)) {
			newSequence = seq.CHARS_GROUP_1_SEQUENCE;
		} else {
			return false;
		}

		reset();
		digitSequence = newSequence;
		loadSpecialCharacters();
		onSuggestionsUpdated.run();
		return true;
	}

	/**
	 * loadSuggestions
	 * Loads the suggestions based on the current state, with optional "currentWord" filter.
	 * Once loading is finished the respective InputMode child will call the Runnable set with
	 * "setOnSuggestionsUpdated()", notifying it the suggestions are available using "getSuggestions()".
	 */
	public void loadSuggestions(String currentWord) {
		onSuggestionsUpdated.run();
	}

	@NonNull
	public ArrayList<String> getSuggestions() {
		// The new list prevents concurrent modification. With a maximum size of 20 Strings, copying
		// should take microseconds, so any performance impact is negligible.
		ArrayList<String> thisThreadSuggestions = new ArrayList<>(suggestions);
		ArrayList<String> newSuggestions = new ArrayList<>(thisThreadSuggestions.size());
		for (String s : thisThreadSuggestions) {
			newSuggestions.add(adjustSuggestionTextCase(s, textCase));
		}

		return newSuggestions;
	}

	public boolean noSuggestions() {
		return suggestions.isEmpty();
	}

	/**
	 * Returns the index of the most appropriate suggestion to be pre-selected. If the mode does not
	 * support recommendation, it should return 0, meaning the first suggestion.
	 */
	public int getRecommendedSuggestionIdx() { return 0; }

	public InputMode setOnSuggestionsUpdated(@NonNull Runnable onSuggestionsUpdated) {
		this.onSuggestionsUpdated = onSuggestionsUpdated;
		return this;
	}

	// Utility
	abstract public int getId();
	public boolean containsGeneratedSuggestions() { return false; }
	public boolean isTyping() { return !digitSequence.isEmpty(); }
	public int getFirstKey() { return digitSequence.isEmpty() ? -1 : digitSequence.charAt(0) - '0'; }
	public int getSequenceLength() { return digitSequence.length(); } // The number of key presses for the current word.
	public int getAutoAcceptTimeout() { return autoAcceptTimeout; }
	public void setSequence(@NonNull String sequence) { digitSequence = sequence; }

	/**
	 * Switches to a new language if the input mode supports it. If the InputMode return "false",
	 * it does not support that language, so you must obtain a compatible alternative using the
	 * getInstance() method and the same ID.
	 * The default implementation is to switch to the new language (including NullLanguage) and
	 * return "true".
	 */
	protected boolean setLanguage(@Nullable Language newLanguage) {
		language = newLanguage != null ? newLanguage : new NullLanguage();
		return true;
	}


	// Interaction with the IME. Return "true" if it should perform the respective action.
	public boolean shouldAcceptPreviousSuggestion(String unacceptedText) { return false; }
	public boolean shouldAcceptPreviousSuggestion(String currentWord, int nextKey, boolean hold) { return false; }
	public boolean shouldReplacePreviousSuggestion(@Nullable String currentWord) { return Characters.PLACEHOLDER.equals(currentWord); }
	public boolean shouldAddTrailingSpace(@NonNull String previousChars, @NonNull String nextChars, boolean isWordAcceptedManually, int nextKey) { return false; }
	public boolean shouldAddPrecedingSpace(@NonNull String previousChars) { return false; }
	public boolean shouldDeletePrecedingSpace(@NonNull String previousChars) { return false; }
	public boolean shouldIgnoreText(String text) { return text == null || text.isEmpty(); }
	public boolean shouldSelectNextSuggestion() { return false; }

	public void reset() {
		autoAcceptTimeout = -1;
		suggestions.clear();
	}

	// recomposing
	public void beforeDeleteText() {}
	public String recompose() { return null; }
	@NonNull public String getRecomposingSuffix() { return ""; }


	// Text case
	public int getTextCase() { return textCase; }

	public boolean setTextCase(int newTextCase) {
		if (!allowedTextCases.contains(newTextCase)) {
			return false;
		}

		textCase = newTextCase;
		return true;
	}

	public void defaultTextCase() {
		textCase = allowedTextCases.get(0);
	}

	/**
	 * Switches to the next available text case. Returns "false" when the language has no upper case.
	 * If "analyzeSurroundingText" is true, and when the mode supports text analyzing, it may apply
	 * additional logic to determine the next valid text case.
	 */
	public boolean nextTextCase(@Nullable String currentWord, int displayTextCase) {
		if (!language.hasUpperCase()) {
			return false;
		}

		int nextIndex = (allowedTextCases.indexOf(textCase) + 1) % allowedTextCases.size();
		textCase = allowedTextCases.get(nextIndex);

		return true;
	}

	public void determineNextWordTextCase(@Nullable String beforeCursor, int nextDigit) {}
	public void skipNextTextCaseDetection() {}

	// Based on the internal logic of the mode (punctuation or grammar rules), re-adjust the text case for when getSuggestions() is called.
	protected String adjustSuggestionTextCase(String word, int newTextCase) { return word; }


	/**
	 * Loads the special characters for 0-key or 1-key. For 0-key, this could be a minimized (show more)
	 * special character list, or the whitespace list.
	 */
	protected boolean loadSpecialCharacters() {
		suggestions.clear();

		if (digitSequence.equals(seq.CHARS_0_SEQUENCE) || digitSequence.equals(seq.CHARS_1_SEQUENCE)) {
			suggestions.addAll(settings.getOrderedKeyChars(language, digitSequence.charAt(0) - '0'));
		} else if (digitSequence.equals(seq.CHARS_GROUP_0_SEQUENCE)) {
			suggestions.addAll(settings.getCharsExtraAsList(language, SettingsStore.CHARS_GROUP_0));
		} else if (digitSequence.equals(seq.CHARS_GROUP_1_SEQUENCE)) {
			suggestions.addAll(settings.getCharsExtraAsList(language, SettingsStore.CHARS_GROUP_1));
		}

		return true;
	}


	// Stem filtering.
	// Where applicable, return "true" if the mode supports it and the operation was possible.
	public boolean clearWordStem() { return setWordStem("", true); }
	public boolean isStemFilterFuzzy() { return false; }
	public String getWordStem() { return ""; }
	public boolean setWordStem(String stem, boolean exact) { return false; }
	public boolean supportsFiltering() { return false; }
}
