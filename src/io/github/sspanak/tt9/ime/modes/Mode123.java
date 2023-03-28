package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Mode123 extends InputMode {
	public int getId() { return MODE_123; }

	Mode123() {
		allowedTextCases.add(CASE_LOWER);
	}


	public boolean onNumber(int key, boolean hold, int repeat) {
		if (key != 0) {
			return false;
		}

		suggestions = new ArrayList<>();
		word = hold ? "+" : "0";
		return true;
	}


	final public boolean is123() { return true; }
	public int getSequenceLength() { return 0; }
	public boolean shouldTrackNumPress() { return false; }

	@NonNull
	@Override
	public String toString() {
		return "[ 123 ]";
	}
}
