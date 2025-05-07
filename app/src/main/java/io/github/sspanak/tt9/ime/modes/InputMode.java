package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NullLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

abstract public class InputMode {
	// typing mode
	public static final int MODE_PREDICTIVE = 0;
	public static final int MODE_ABC = 1;
	public static final int MODE_123 = 2;
	public static final int MODE_PASSTHROUGH = 4;
	public static final int MODE_HIRAGANA = 5;
	public static final int MODE_KATAKANA = 6;

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
	protected int specialCharSelectedGroup = 0;


	protected InputMode(SettingsStore settings, InputType inputType) {
		allowedTextCases.add(CASE_LOWER);
		isEmailMode = inputType != null && inputType.isEmail() && !inputType.isDefectiveText();
		this.settings = settings;
	}


	public static InputMode getInstance(SettingsStore settings, @Nullable Language language, InputType inputType, TextField textField, int mode) {
		switch (mode) {
			case MODE_PREDICTIVE:
				if (LanguageKind.isChineseBopomofo(language)) return new ModeBopomofo(settings, language, inputType, textField);
				if (LanguageKind.isChinesePinyin(language)) return new ModePinyin(settings, language, inputType, textField);
				if (LanguageKind.isJapanese(language)) return new ModeKanji(settings, language, inputType, textField);
				if (LanguageKind.isKorean(language)) return new ModeCheonjiin(settings, inputType, textField);
				if (language != null && language.isTranscribed()) return new ModeIdeograms(settings, language, inputType, textField);
				return new ModeWords(settings, language, inputType, textField);
			case MODE_HIRAGANA:
				if (LanguageKind.isJapanese(language)) return new ModeHiragana(settings, language, inputType, textField);
				return new ModeABC(settings, language, inputType);
			case MODE_KATAKANA:
				if (LanguageKind.isJapanese(language)) return new ModeKatakana(settings, language, inputType, textField);
				return new ModeABC(settings, language, inputType);
			case MODE_ABC:
				return new ModeABC(settings, language, inputType);
			case MODE_PASSTHROUGH:
				return new ModePassthrough(settings, inputType);
			default:
				Logger.w("InputMode", "Defaulting to mode: " + Mode123.class.getName() + " for unknown InputMode: " + mode);
			case MODE_123:
				return new Mode123(settings, language, inputType);
		}
	}

	// Key handlers. Return "true" when handling the key or "false", when is nothing to do.
	public boolean onBackspace() { return false; }
	abstract public boolean onNumber(int number, boolean hold, int repeat);

	// Suggestions
	public void onAcceptSuggestion(@NonNull String word) { onAcceptSuggestion(word, false); }
	public void onAcceptSuggestion(@NonNull String word, boolean preserveWordList) {}
	public void onCursorMove(@NonNull String word) { if (!digitSequence.isEmpty()) onAcceptSuggestion(word); }
	public void onReplaceSuggestion(@NonNull String word) {}

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
		ArrayList<String> newSuggestions = new ArrayList<>();
		for (String s : suggestions) {
			newSuggestions.add(adjustSuggestionTextCase(s, textCase));
		}

		return newSuggestions;
	}

	public InputMode setOnSuggestionsUpdated(@NonNull Runnable onSuggestionsUpdated) {
		this.onSuggestionsUpdated = onSuggestionsUpdated;
		return this;
	}

	// Utility
	abstract public int getId();
	public boolean containsGeneratedSuggestions() { return false; }
	@NonNull public String getSequence() { return digitSequence; }
	public void setSequence(@NonNull String sequence) { digitSequence = sequence; }
	public int getSequenceLength() { return digitSequence.length(); } // The number of key presses for the current word.
	public int getAutoAcceptTimeout() {
		return autoAcceptTimeout;
	}

	/**
	 * Switches to a new language if the input mode supports it. If the InputMode return "false",
	 * it does not support that language, so you must obtain a compatible alternative using the
	 * getInstance() method and the same ID.
	 * The default implementation is to switch to the new language (including NullLanguage) and
	 * return "true".
	 */
	public boolean changeLanguage(@Nullable Language newLanguage) {
		setLanguage(newLanguage);
		return true;
	}

	protected void setLanguage(@Nullable Language newLanguage) {
		language = newLanguage != null ? newLanguage : new NullLanguage();
	}


	// Interaction with the IME. Return "true" if it should perform the respective action.
	public boolean shouldAcceptPreviousSuggestion(String unacceptedText) { return false; }
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) { return false; }
	public boolean shouldReplacePreviousSuggestion() { return false; }
	public boolean shouldAddTrailingSpace(boolean isWordAcceptedManually, int nextKey) { return false; }
	public boolean shouldAddPrecedingSpace() { return false; }
	public boolean shouldDeletePrecedingSpace() { return false; }
	public boolean shouldIgnoreText(String text) { return text == null || text.isEmpty(); }
	public boolean shouldReplaceLastLetter(int nextKey, boolean hold) { return false; }
	public boolean shouldSelectNextSuggestion() { return false; }

	public String recompose() { return null; }
	public void replaceLastLetter() {}

	public void reset() {
		autoAcceptTimeout = -1;
		specialCharSelectedGroup = 0;
		suggestions.clear();
	}

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

	public boolean nextTextCase() {
		if (!language.hasUpperCase()) {
			return false;
		}

		int nextIndex = (allowedTextCases.indexOf(textCase) + 1) % allowedTextCases.size();
		textCase = allowedTextCases.get(nextIndex);

		return true;
	}

	public void determineNextWordTextCase() {}

	// Based on the internal logic of the mode (punctuation or grammar rules), re-adjust the text case for when getSuggestions() is called.
	protected String adjustSuggestionTextCase(String word, int newTextCase) { return word; }


	protected boolean loadSpecialCharacters() {
		int key = digitSequence.charAt(0) - '0';
		ArrayList<String> chars = settings.getOrderedKeyChars(language, key, specialCharSelectedGroup);

		if (chars.isEmpty() && specialCharSelectedGroup == 1) {
			specialCharSelectedGroup = 0;
			return false;
		} else if (chars.isEmpty()) {
			specialCharSelectedGroup = 0;
			chars = settings.getOrderedKeyChars(language, key, specialCharSelectedGroup);
		}

		suggestions.clear();
		suggestions.addAll(chars);

		return true;
	}


	/**
	 * Applies the punctuation order when we don't want to display the entire
	 * list of characters, for example in email, numeric or other specialized fields.
	 */
	protected ArrayList<String> applyPunctuationOrder(ArrayList<String> unordered, int key) {
		if (specialCharSelectedGroup != 0 || key > 1) {
			return new ArrayList<>(unordered);
		}

		ArrayList<String> ordered = new ArrayList<>();

		if (isEmailMode) {
			if (unordered.contains("@")) ordered.add("@");
			if (unordered.contains("_")) ordered.add("_");
		}

		for (String ch : settings.getOrderedKeyChars(language, key)) {
			if (isEmailMode && (ch.charAt(0) == '@' || ch.charAt(0) == '_')) {
				continue;
			}

			if (unordered.contains(ch)) {
				ordered.add(ch);
			}
		}

		return ordered;
	}


	// Stem filtering.
	// Where applicable, return "true" if the mode supports it and the operation was possible.
	public boolean clearWordStem() { return setWordStem("", true); }
	public boolean isStemFilterFuzzy() { return false; }
	public String getWordStem() { return ""; }
	public boolean setWordStem(String stem, boolean exact) { return false; }
	public boolean supportsFiltering() { return false; }
}
