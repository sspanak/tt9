package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

// see: InputType.isSpecialNumeric()
class ModePassthrough extends InputMode {
	protected ModePassthrough(SettingsStore settings, InputType inputType) {
		super(settings, inputType);
		reset();
	}

	@Override public int getId() { return MODE_PASSTHROUGH; }
	@Override public int getSequenceLength() { return 0; }
	@Override @NonNull public String toString() { return "--"; }

	@Override public boolean onNumber(int n, boolean h, int r, @NonNull String[] s) { return false; }
	@Override public boolean shouldIgnoreText(String t) { return true; }
}
