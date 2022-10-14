package io.github.sspanak.tt9.ime.modes;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;

public class Mode123 extends InputMode {
	public int getId() { return MODE_123; }

	Mode123() {
		allowedTextCases.add(CASE_LOWER);
	}


	public boolean onNumber(Language l, int key, boolean hold, boolean repeat) {
		if (key != 0) {
			return false;
		}

		suggestions = new ArrayList<>();
		word = hold ? "+" : "0";
		return true;
	}


	final public boolean is123() { return true; }
	public boolean shouldTrackNumPress() { return false; }
}
