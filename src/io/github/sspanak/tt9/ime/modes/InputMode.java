package io.github.sspanak.tt9.ime.modes;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;

abstract public class InputMode {
	// typing mode
	public static final int MODE_PREDICTIVE = 0;
	public static final int MODE_ABC = 1;
	public static final int MODE_123 = 2;

	// text case
	public static final int CASE_UPPER = 0;
	public static final int CASE_CAPITALIZE = 1;
	public static final int CASE_LOWER = 2;
	protected ArrayList<Integer> allowedTextCases = new ArrayList<>();

	// data
	protected ArrayList<String> suggestions = new ArrayList<>();
	protected String word = null;


	public static InputMode getInstance(int mode) {
		switch(mode) {
			case MODE_PREDICTIVE:
				return new ModePredictive();
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
	abstract public boolean onNumber(Language language, int key, boolean hold, boolean repeat);

	// Suggestions
	public void onAcceptSuggestion(Language language, String suggestion) {}
	public ArrayList<String> getSuggestions() { return suggestions; }
	public boolean getSuggestionsAsync(Handler handler, Language language, String lastWord) { return false; }
	protected void sendSuggestions(Handler handler, ArrayList<String> suggestions) {
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("suggestions", suggestions);
		Message msg = new Message();
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	// Word
	public String getWord() { return word; }

	// Mode identifiers
	public boolean isPredictive() { return false; }
	public boolean isABC() { return false; }
	public boolean is123() { return false; }

	// Utility
	abstract public int getId();
	public ArrayList<Integer> getAllowedTextCases() { return allowedTextCases; }
	// Perform any special logic to determine the text case of the next word, or return "-1" if there is no need to change it.
	public int getNextWordTextCase(int currentTextCase, boolean isThereText, String textBeforeCursor) { return -1; }
	abstract public int getSequenceLength(); // The number of key presses for the current word.
	public void reset() {
		suggestions = new ArrayList<>();
		word = null;
	}

	// Stem filtering.
	// Where applicable, return "true" if the mode supports it and the operation was possible.
	public boolean isStemFilterOn() { return false; }
	public void clearStemFilter() {}
	public boolean setStemFilter(Language language, String stem) { return false; }

	public boolean shouldTrackNumPress() { return true; }
	public boolean shouldTrackUpDown() { return false; }
	public boolean shouldTrackLeftRight() { return false; }
	public boolean shouldAcceptCurrentSuggestion(Language language, int key, boolean hold, boolean repeat) { return false; }
	public boolean shouldSelectNextSuggestion() { return false; }
}
