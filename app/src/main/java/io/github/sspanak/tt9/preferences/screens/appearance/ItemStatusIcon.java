package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public record ItemStatusIcon(SwitchPreferenceCompat item, SettingsStore settings) {
	public static final String NAME = "pref_status_icon";

	public void populate() {
		if (item != null) {
			item.setChecked(settings.isStatusIconEnabled());
		}
	}
}
