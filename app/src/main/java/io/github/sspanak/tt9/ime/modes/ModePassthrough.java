package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

// see: InputType.isSpecialNumeric()
public class ModePassthrough extends InputMode {
	ModePassthrough() {
		reset();
		allowedTextCases.add(CASE_LOWER);
	}

	@Override public int getId() { return MODE_PASSTHROUGH; }
	@Override public int getIcon() { return 0; }
	@Override public int getSequenceLength() { return 0; }
	@Override @NonNull public String toString() { return "--"; }

	@Override public boolean isNumeric() { return true; }
	@Override public boolean isPassthrough() { return true; }

	@Override public boolean onNumber(int number, boolean hold, int repeat) { return false; }
	@Override public boolean shouldIgnoreText(String text) { return true; }
}
