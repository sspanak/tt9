package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

abstract public class InputMode {
	// typing mode
	public static final int MODE_PREDICTIVE = 0;
	public static final int MODE_ABC = 1;
	public static final int MODE_123 = 2;
	public static final int MODE_PASSTHROUGH = 4;

	// text case
	public static final int CASE_UNDEFINED = -1;
	public static final int CASE_UPPER = 0;
	public static final int CASE_CAPITALIZE = 1;
	public static final int CASE_LOWER = 2;
	public static final int CASE_DICTIONARY = 3; // do not force it, but use the dictionary word as-is
	protected final ArrayList<Integer> allowedTextCases = new ArrayList<>();
	protected int textCase = CASE_LOWER;
	protected int textFieldTextCase = CASE_UNDEFINED;

	// data
	protected int autoAcceptTimeout = -1;
	@NonNull protected String digitSequence = "";
	protected Language language;
	protected final ArrayList<String> suggestions = new ArrayList<>();
	@NonNull protected Runnable onSuggestionsUpdated = () -> {};
	protected int specialCharSelectedGroup = 0;


	public static InputMode getInstance(SettingsStore settings, Language language, InputType inputType, int mode) {
		switch (mode) {
			case MODE_PREDICTIVE:
				return new ModePredictive(settings, inputType, language);
			case MODE_ABC:
				return new ModeABC(settings, inputType, language);
			case MODE_PASSTHROUGH:
				return new ModePassthrough();
			default:
				Logger.w("InputMode", "Defaulting to mode: " + Mode123.class.getName() + " for unknown InputMode: " + mode);
			case MODE_123:
				return new Mode123(inputType, language);
		}
	}

	// Key handlers. Return "true" when handling the key or "false", when is nothing to do.
	public boolean onBackspace() { return false; }
	abstract public boolean onNumber(int number, boolean hold, int repeat);

	// Suggestions
	public void onAcceptSuggestion(@NonNull String word) { onAcceptSuggestion(word, false); }
	public void onAcceptSuggestion(@NonNull String word, boolean preserveWordList) {}

	/**
	 * loadSuggestions
	 * Loads the suggestions based on the current state, with optional "currentWord" filter.
	 * Once loading is finished the respective InputMode child will call the Runnable set with
	 * "setOnSuggestionsUpdated()", notifying it the suggestions are available using "getSuggestions()".
	 */
	public void loadSuggestions(String currentWord) {
		onSuggestionsUpdated.run();
	}

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

	// Numeric mode identifiers. "instanceof" cannot be used in all cases, because they inherit each other.
	public boolean is123() { return false; }
	public boolean isPassthrough() { return false; }
	public boolean isNumeric() { return false; }

	// Utility
	abstract public int getId();
	public boolean containsGeneratedSuggestions() { return false; }
	public String getSequence() { return digitSequence; }
	public int getSequenceLength() { return digitSequence.length(); } // The number of key presses for the current word.
	public int getAutoAcceptTimeout() {
		return autoAcceptTimeout;
	}
	public void changeLanguage(Language newLanguage) {
		if (newLanguage != null) {
			language = newLanguage;
		}
	}

	// Interaction with the IME. Return "true" if it should perform the respective action.
	public boolean shouldAcceptPreviousSuggestion() { return false; }
	public boolean shouldAcceptPreviousSuggestion(int nextKey) { return false; }
	public boolean shouldAddAutoSpace(InputType inputType, TextField textField, boolean isWordAcceptedManually, int nextKey) { return false; }
	public boolean shouldDeletePrecedingSpace(InputType inputType) { return false; }
	public boolean shouldIgnoreText(String text) { return text == null || text.isEmpty(); }
	public boolean shouldSelectNextSuggestion() { return false; }
	public boolean recompose(String word) { return false; }

	public void reset() {
		autoAcceptTimeout = -1;
		specialCharSelectedGroup = -1;
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

	public void setTextFieldCase(int newTextCase) {
		textFieldTextCase = allowedTextCases.contains(newTextCase) ? newTextCase : CASE_UNDEFINED;
	}

	public void defaultTextCase() {
		textCase = allowedTextCases.get(0);
	}

	public boolean nextTextCase() {
		if (nextSpecialCharacters()) {
			return true;
		}

		if (!language.hasUpperCase() || digitSequence.startsWith(NaturalLanguage.PUNCTUATION_KEY) || digitSequence.startsWith(NaturalLanguage.SPECIAL_CHARS_KEY)) {
			return false;
		}

		int nextIndex = (allowedTextCases.indexOf(textCase) + 1) % allowedTextCases.size();
		textCase = allowedTextCases.get(nextIndex);

		return true;
	}

	public void determineNextWordTextCase(String textBeforeCursor) {}

	// Based on the internal logic of the mode (punctuation or grammar rules), re-adjust the text case for when getSuggestions() is called.
	protected String adjustSuggestionTextCase(String word, int newTextCase) { return word; }


	/**
	 * This is used in nextTextCase() for switching to the next set of characters. Obviously,
	 * special chars do not have a text case, but we use this trick to alternate the char groups.
	 */
	protected boolean nextSpecialCharacters() { return nextSpecialCharacters(language); }
	protected boolean nextSpecialCharacters(Language altLanguage) {
		int previousGroup = specialCharSelectedGroup;
		specialCharSelectedGroup++;

		return
			loadSpecialCharacters(altLanguage) // validates specialCharSelectedGroup
			&& previousGroup != specialCharSelectedGroup; // verifies validation has passed
	}

	protected boolean loadSpecialCharacters(Language altLanguage) {
		if (altLanguage == null || digitSequence.isEmpty()) {
			return false;
		}

		int key = digitSequence.charAt(0) - '0';
		ArrayList<String> chars = altLanguage.getKeyCharacters(key, specialCharSelectedGroup);

		if (chars.isEmpty() && specialCharSelectedGroup == 1) {
			specialCharSelectedGroup = 0;
			return false;
		} else if (chars.isEmpty()) {
			specialCharSelectedGroup = 0;
			chars = altLanguage.getKeyCharacters(key, specialCharSelectedGroup);
		}

		suggestions.clear();
		suggestions.addAll(chars);

		return true;
	}

	// Stem filtering.
	// Where applicable, return "true" if the mode supports it and the operation was possible.
	public boolean clearWordStem() { return setWordStem("", true); }
	public boolean isStemFilterFuzzy() { return false; }
	public String getWordStem() { return ""; }
	public boolean setWordStem(String stem, boolean exact) { return false; }
}
