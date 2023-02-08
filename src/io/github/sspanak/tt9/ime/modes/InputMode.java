package io.github.sspanak.tt9.ime.modes;

import android.os.Handler;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

abstract public class InputMode {
	// typing mode
	public static final int MODE_PREDICTIVE = 0;
	public static final int MODE_ABC = 1;
	public static final int MODE_123 = 2;

	// text case
	public static final int CASE_UNDEFINED = -1;
	public static final int CASE_UPPER = 0;
	public static final int CASE_CAPITALIZE = 1;
	public static final int CASE_LOWER = 2;
	public static final int CASE_DICTIONARY = 3; // do not force it, but use the dictionary word as-is
	protected ArrayList<Integer> allowedTextCases = new ArrayList<>();
	protected int textCase = CASE_LOWER;
	protected int textFieldTextCase = CASE_UNDEFINED;

	// data
	protected ArrayList<String> suggestions = new ArrayList<>();
	protected String word = null;


	public static InputMode getInstance(SettingsStore settings, int mode) {
		switch (mode) {
			case MODE_PREDICTIVE:
				return new ModePredictive(settings);
			case MODE_ABC:
				return new ModeABC();
			default:
				Logger.w("tt9/InputMode", "Defaulting to mode: " + Mode123.class.getName() + " for unknown InputMode: " + mode);
			case MODE_123:
				return new Mode123();
		}
	}

	// Key handlers. Return "true" when handling the key or "false", when is nothing to do.
	public boolean onBackspace() { return false; }
	abstract public boolean onNumber(Language language, int key, boolean hold, int repeat);

	// Suggestions
	public void onAcceptSuggestion(Language language, String suggestion) {}
	protected void onSuggestionsUpdated(Handler handler) { handler.sendEmptyMessage(0); }
	public boolean loadSuggestions(Handler handler, Language language, String currentWord) { return false; }

	public ArrayList<String> getSuggestions(Language language) {
		ArrayList<String> newSuggestions = new ArrayList<>();
		for (String s : suggestions) {
			newSuggestions.add(adjustSuggestionTextCase(s, textCase, language));
		}

		return newSuggestions;
	}

	// Word
	public String getWord() { return word; }

	// Mode identifiers
	public boolean isPredictive() { return false; }
	public boolean isABC() { return false; }
	public boolean is123() { return false; }

	// Utility
	abstract public int getId();
	abstract public int getSequenceLength(); // The number of key presses for the current word.

	// Interaction with the IME. Return "true" if it should perform the respective action.
	public boolean shouldAcceptCurrentSuggestion(Language language, int key, boolean hold, boolean repeat) { return false; }
	public boolean shouldAddAutoSpace(InputConnection inputConnection, EditorInfo inputField, boolean isWordAcceptedManually, int incomingKey, boolean hold, boolean repeat) { return false; }
	public boolean shouldDeletePrecedingSpace(EditorInfo inputField) { return false; }
	public boolean shouldSelectNextSuggestion() { return false; }
	public boolean shouldTrackNumPress() { return true; }
	public boolean shouldTrackUpDown() { return false; }
	public boolean shouldTrackLeftRight() { return false; }

	public void reset() {
		suggestions = new ArrayList<>();
		word = null;
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

	public void determineNextWordTextCase(SettingsStore settings, boolean isThereText, String textBeforeCursor) {}

	// Based on the internal logic of the mode (punctuation or grammar rules), re-adjust the text case for when getSuggestions() is called.
	protected String adjustSuggestionTextCase(String word, int newTextCase, Language language) { return word; }

	// Stem filtering.
	// Where applicable, return "true" if the mode supports it and the operation was possible.
	public boolean clearWordStem() { return false; }
	public boolean isStemFilterFuzzy() { return false; }
	public String getWordStem() { return ""; }
	public boolean setWordStem(Language language, String stem, boolean exact) { return false; }
}
