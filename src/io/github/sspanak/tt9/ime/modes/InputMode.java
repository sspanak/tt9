package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.ime.helpers.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

abstract public class InputMode {
	// typing mode
	public static final int MODE_UNDEFINED = -1;
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
	protected Language language;
	protected final ArrayList<String> suggestions = new ArrayList<>();
	protected int keyCode = 0;


	public static InputMode getInstance(SettingsStore settings, Language language, int mode) {
		switch (mode) {
			case MODE_PREDICTIVE:
				return new ModePredictive(settings, language);
			case MODE_ABC:
				return new ModeABC(settings, language);
			case MODE_PASSTHROUGH:
				return new ModePassthrough();
			default:
				Logger.w("tt9/InputMode", "Defaulting to mode: " + Mode123.class.getName() + " for unknown InputMode: " + mode);
			case MODE_123:
				return new Mode123();
		}
	}

	// Key handlers. Return "true" when handling the key or "false", when is nothing to do.
	public boolean onBackspace() { return false; }
	abstract public boolean onNumber(int number, boolean hold, int repeat);
	abstract public boolean onOtherKey(int key);

	// Suggestions
	public void onAcceptSuggestion(@NonNull String word) { onAcceptSuggestion(word, false); }
	public void onAcceptSuggestion(@NonNull String word, boolean preserveWordList) {}

	/**
	 * loadSuggestions
	 * Loads the suggestions based on the current state, with optional "currentWord" filter.
	 * Once loading is finished the respective InputMode child will call "onLoad", notifying it
	 * the suggestions are available using "getSuggestions()".
	 */
	public void loadSuggestions(Runnable onLoad, String currentWord) {
		onLoad.run();
	}

	public ArrayList<String> getSuggestions() {
		ArrayList<String> newSuggestions = new ArrayList<>();
		for (String s : suggestions) {
			newSuggestions.add(adjustSuggestionTextCase(s, textCase));
		}

		return newSuggestions;
	}

	// Mode identifiers
	public boolean isABC() { return false; }
	public boolean is123() { return false; }
	public boolean isPassthrough() { return false; }
	public boolean isNumeric() { return false; }

	// Utility
	abstract public int getId();
	abstract public int getSequenceLength(); // The number of key presses for the current word.
	public int getAutoAcceptTimeout() {
		return autoAcceptTimeout;
	}
	public int getKeyCode() { return keyCode; }
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
	public boolean shouldSelectNextSuggestion() { return false; }

	public boolean shouldTrackUpDown() { return false; }
	public boolean shouldTrackLeftRight() { return false; }

	public void reset() {
		autoAcceptTimeout = -1;
		keyCode = 0;
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

	public void nextTextCase() {
		int nextIndex = (allowedTextCases.indexOf(textCase) + 1) % allowedTextCases.size();
		textCase = allowedTextCases.get(nextIndex);
	}

	public void determineNextWordTextCase(boolean isThereText, String textBeforeCursor) {}

	// Based on the internal logic of the mode (punctuation or grammar rules), re-adjust the text case for when getSuggestions() is called.
	protected String adjustSuggestionTextCase(String word, int newTextCase) { return word; }

	// Stem filtering.
	// Where applicable, return "true" if the mode supports it and the operation was possible.
	public boolean clearWordStem() { return false; }
	public boolean isStemFilterFuzzy() { return false; }
	public String getWordStem() { return ""; }
	public boolean setWordStem(String stem, boolean exact) { return false; }
}
