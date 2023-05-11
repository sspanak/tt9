package io.github.sspanak.tt9.ime.modes;

import android.view.KeyEvent;

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
		keyCode = (number == 0 && hold) ? KeyEvent.KEYCODE_PLUS : Key.numberToCode(number);
		return true;
	}

	@Override
	public boolean onOtherKey(int key) {
		reset();
		if (Key.isDecimalSeparator(key) || Key.isPoundOrStar(key)) {
			keyCode = key;
			return true;
		}

		return false;
	}

	@Override final public boolean is123() { return true; }
	@Override public int getSequenceLength() { return 0; }

	@NonNull
	@Override
	public String toString() {
		return "123";
	}
}
