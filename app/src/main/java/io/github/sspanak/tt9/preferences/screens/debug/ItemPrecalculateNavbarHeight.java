package io.github.sspanak.tt9.preferences.screens.debug;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemPrecalculateNavbarHeight extends ItemClickable {
	public static final String NAME = "hack_precalculate_navbar_height_v3";

	private final SettingsStore settings;

	public ItemPrecalculateNavbarHeight(@NonNull SettingsStore settings, Preference item) {
		super(item);
		this.settings = settings;
	}

	@Override
	protected boolean onClick(Preference p) {
		return true;
	}

	void populate() {
		if (item != null) {
			((SwitchPreferenceCompat) item).setChecked(settings.getPrecalculateNavbarHeight());
			item.setVisible(DeviceInfo.AT_LEAST_ANDROID_15);
		}
	}
}
