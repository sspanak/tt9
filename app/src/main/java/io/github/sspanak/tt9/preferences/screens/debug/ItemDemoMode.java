package io.github.sspanak.tt9.preferences.screens.debug;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;

class ItemDemoMode extends ItemClickable {
	public static final String NAME = "pref_demo_mode";

	private final PreferencesActivity activity;

	ItemDemoMode(Preference item, PreferencesActivity activity) {
		super(item);
		this.activity = activity;
	}

	@Override
	protected boolean onClick(Preference p) {
		activity.getSettings().setDemoMode(((SwitchPreferenceCompat) p).isChecked());
		activity.getOnBackPressedDispatcher().onBackPressed();
		return true;
	}

	ItemDemoMode populate() {
		if (item != null) {
			((SwitchPreferenceCompat) item).setChecked(activity.getSettings().getDemoMode());
		}

		return this;
	}
}
