package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemStatusIcon {
	public static final String NAME = "pref_status_icon";

	private final SwitchPreferenceCompat item;
	private final SettingsStore settings;

	public ItemStatusIcon(SwitchPreferenceCompat item, SettingsStore settings) {
		this.item = item;
		this.settings = settings;
		addAppNameToSummary();
	}

	public void populate() {
		if (item != null) {
			item.setChecked(settings.isStatusIconEnabled());
		}
	}

	private void addAppNameToSummary() {
		if (item == null) {
			return;
		}

		String summary = item.getContext().getString(R.string.pref_status_icon_summary, item.getContext().getString(R.string.app_name));
		item.setSummary(summary);
	}
}
