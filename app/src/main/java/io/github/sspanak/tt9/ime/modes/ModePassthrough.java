package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

// see: InputType.isSpecialNumeric()
public class ModePassthrough extends InputMode {
	ModePassthrough(SettingsStore settings, InputType inputType) {
		super(settings, inputType);
		reset();
		allowedTextCases.add(CASE_LOWER);
	}

	@Override public int getId() { return MODE_PASSTHROUGH; }
	@Override public int getSequenceLength() { return 0; }
	@Override @NonNull public String toString() { return "--"; }

	@Override public boolean onNumber(int number, boolean hold, int repeat) { return false; }
	@Override public boolean shouldIgnoreText(String text) { return true; }
}
