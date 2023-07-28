package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

public class Mode123 extends ModePassthrough {
	@Override public int getId() { return MODE_123; }
	@Override @NonNull public String toString() { return "123"; }

	@Override public final boolean is123() { return true; }
	@Override public boolean isPassthrough() { return false; }

	@Override public void reset() {
		super.reset();
		autoAcceptTimeout = 0;
	}

	@Override public boolean onNumber(int number, boolean hold, int repeat) {
		reset();
		suggestions.add((number == 0 && hold) ? "+" : String.valueOf(number));
		return true;
	}

	/**
	 * shouldIgnoreText
	 * Since this is a numeric mode, we allow typing only numbers and:
	 * 	1. In numeric fields, we must allow math chars
	 * 	2. In dialer fields, we must allow various punctuation chars, because they are used as dialing shortcuts
	 * 	at least in Japan.
	 * More info and discussion: <a href="https://github.com/sspanak/tt9/issues/241">issue 241 on Github</a>.
	 */
	@Override public boolean shouldIgnoreText(String text) {
		return
			text == null
			|| text.length() != 1
			|| !(
				(text.charAt(0) > 31 && text.charAt(0) < 65)
				|| (text.charAt(0) > 90 && text.charAt(0) < 97)
				|| (text.charAt(0) > 122 && text.charAt(0) < 127)
			);
	}
}
