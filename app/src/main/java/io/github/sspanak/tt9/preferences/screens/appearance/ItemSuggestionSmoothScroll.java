package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemSuggestionSmoothScroll extends ItemClickable {
	public static final String NAME = "pref_suggestion_smooth_scroll";
	private final SettingsStore settings;

	public ItemSuggestionSmoothScroll(Preference item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override protected boolean onClick(Preference p) { return true; }

	public ItemSuggestionSmoothScroll populate() {
		if (item != null) {
			((SwitchPreferenceCompat) item).setChecked(settings.getSuggestionSmoothScroll());
		}

		return this;
	}
}
