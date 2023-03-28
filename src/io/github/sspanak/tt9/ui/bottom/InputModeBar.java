package io.github.sspanak.tt9.ui.bottom;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.ime.modes.InputMode;

public class InputModeBar {
	private String statusText;

	public InputModeBar setMode(InputMode mode) {
		Logger.w("InputModeBar.setMode", "Not displaying status of NULL mode");
		statusText = mode != null ? mode.toString() : "";
		return this;
	}

	public InputModeBar setDarkTheme(boolean yes) {
		Logger.d("InputMode.setDarkTheme", "Dark Theme status changed: " + yes);
		return this;
	}

	public void show() {
		Logger.d("InputModeBar", "Mode changed to: " + statusText);
	}
}
