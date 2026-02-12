package io.github.sspanak.tt9.ui.main.keys;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ActionKey {
	@NonNull
	public static String numberToActionKey(int number) {
		return switch (number) {
			case 1 -> SettingsStore.CUSTOM_ACTION_KEY_1;
			case 2 -> SettingsStore.CUSTOM_ACTION_KEY_2;
			case 3 -> SettingsStore.CUSTOM_ACTION_KEY_3;
			case 4 -> SettingsStore.CUSTOM_ACTION_KEY_4;
			case 5 -> SettingsStore.CUSTOM_ACTION_KEY_5;
			case 6 -> SettingsStore.CUSTOM_ACTION_KEY_6;
			case 7 -> SettingsStore.CUSTOM_ACTION_KEY_7;
			case 8 -> SettingsStore.CUSTOM_ACTION_KEY_8;
			case 9 -> SettingsStore.CUSTOM_ACTION_KEY_9;
			default -> "";
		};
	}
}
