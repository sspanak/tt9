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
	public static final int CASE_LOWER = 0;
	public static final int CASE_CAPITALIZE = 1;
	public static final int CASE_UPPER = 2;
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
	protected void sendSuggestions(Handler handler, ArrayList<String> suggestions, int maxWordLength) {
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("suggestions", suggestions);
		bundle.putInt("maxWordLength", maxWordLength);
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
	public void reset() {
		suggestions = new ArrayList<>();
		word = null;
	}
	public boolean shouldTrackNumPress() { return true; }
	public boolean shouldAcceptCurrentSuggestion(int key, boolean hold, boolean repeat) { return false; }
	public boolean shouldSelectNextSuggestion() { return false; }
}
