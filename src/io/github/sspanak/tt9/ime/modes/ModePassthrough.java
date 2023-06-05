package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

// see: InputType.isDialer()
public class ModePassthrough extends InputMode {
	ModePassthrough() {
		reset();
		allowedTextCases.add(CASE_LOWER);
	}

	@Override public int getId() { return MODE_PASSTHROUGH; }
	@Override public int getSequenceLength() { return 0; }
	@Override @NonNull public String toString() { return "Passthrough"; }

	@Override public boolean isNumeric() { return true; }
	@Override public boolean isPassthrough() { return true; }

	public boolean onNumber(int number, boolean hold, int repeat) { return false; }
	public boolean onOtherKey(int key) { return false; }
}
