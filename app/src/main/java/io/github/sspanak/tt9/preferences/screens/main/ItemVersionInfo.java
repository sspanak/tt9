package io.github.sspanak.tt9.preferences.screens.main;

import androidx.preference.Preference;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.preferences.screens.debug.DebugScreen;

class ItemVersionInfo extends ItemClickable {
	static final String NAME = "version_info";

	private final PreferencesActivity activity;

	ItemVersionInfo(Preference item, PreferencesActivity activity) {
		super(item);
		this.activity = activity;
	}

	@Override
	protected boolean onClick(Preference p) {
		if (!activity.getSettings().getDemoMode()) {
			activity.displayScreen(DebugScreen.NAME);
		}

		return true;
	}

	ItemVersionInfo populate() {
		if (item != null) {
			item.setSummary(BuildConfig.VERSION_FULL);
		}
		return this;
	}
}
