package io.github.sspanak.tt9.ime.modes;

import io.github.sspanak.tt9.languages.Language;

public class ModeABC extends InputMode {
	public int getId() { return MODE_ABC; }

	private boolean shouldSelectNextLetter = false;

	ModeABC() {
		allowedTextCases.add(CASE_LOWER);
		allowedTextCases.add(CASE_UPPER);
	}


	public boolean onNumber(Language language, int key, boolean hold, boolean repeat) {
		shouldSelectNextLetter = false;
		suggestions = language.getKeyCharacters(key);
		word = null;

		if (hold) {
			suggestions = null;
			word = String.valueOf(key);
		} else if (repeat) {
			suggestions = null;
			shouldSelectNextLetter = true;
		}

		return true;
	}


	final public boolean isABC() { return true; }
	public boolean shouldAcceptCurrentSuggestion(int key, boolean hold, boolean repeat) {	return hold || !repeat;	}
	public boolean shouldSelectNextSuggestion() {
		return shouldSelectNextLetter;
	}
}
