package io.github.sspanak.tt9.preferences.screens.setup;

import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemAlternativeSuggestionScrolling {
	public static final String NAME = "pref_alternative_suggestion_scrolling";

	private final SwitchPreferenceCompat item;
	private final SettingsStore settings;

	public ItemAlternativeSuggestionScrolling(SwitchPreferenceCompat item, SettingsStore settings) {
		this.item = item;
		this.settings = settings;
	}

	public ItemAlternativeSuggestionScrolling populate() {
		if (item != null) {
			item.setChecked(settings.getSuggestionScrollingDelay() > 0);
		}

		return this;
	}

	public void setEnabled(boolean yes) {
		if (item != null) {
			item.setEnabled(yes);
		}
	}
}
