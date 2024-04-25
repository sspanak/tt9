package io.github.sspanak.tt9.ime;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

abstract public class MainViewOps extends HotkeyHandler {
	/**** Informational methods for the on-screen keyboard ****/
	public int getTextCase() {
		return mInputMode.getTextCase();
	}

	public boolean isInputModeNumeric() {
		return mInputMode.is123();
	}

	public boolean isNumericModeStrict() {
		return mInputMode.is123() && inputType.isNumeric() && !inputType.isPhoneNumber();
	}

	public boolean isNumericModeSigned() {
		return mInputMode.is123() && inputType.isSignedNumber();
	}

	public boolean isInputModePhone() {
		return mInputMode.is123() && inputType.isPhoneNumber();
	}

	public SettingsStore getSettings() {
		return settings;
	}
}
