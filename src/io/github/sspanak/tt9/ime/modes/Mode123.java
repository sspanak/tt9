package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ime.helpers.Key;

public class Mode123 extends InputMode {
	public int getId() { return MODE_123; }

	Mode123() {
		allowedTextCases.add(CASE_LOWER);
	}

	@Override
	public boolean onNumber(int number, boolean hold, int repeat) {
		reset();

		if (number == 0 && hold) {
			autoAcceptTimeout = 0;
			suggestions.add("+");
		} else {
			keyCode = Key.numberToCode(number);
		}

		return true;
	}


	@Override final public boolean is123() { return true; }
	@Override public int getSequenceLength() { return 0; }
	@Override public boolean shouldTrackNumPress() { return false; }

	@NonNull
	@Override
	public String toString() {
		return "123";
	}
}
