package io.github.sspanak.tt9.ime.modes;

import android.view.KeyEvent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ime.helpers.Key;

public class Mode123 extends ModePassthrough {
	@Override public int getId() { return MODE_123; }
	@Override @NonNull public String toString() { return "123"; }

	@Override public final boolean is123() { return true; }
	@Override public boolean isPassthrough() { return false; }

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
}
