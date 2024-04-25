package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.preferences.items.ItemSwitch;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemAlternativeSuggestionScrolling extends ItemSwitch {
	public static final String NAME = "pref_alternative_suggestion_scrolling";

	private final SettingsStore settings;

	public ItemAlternativeSuggestionScrolling(SwitchPreferenceCompat item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override
	protected boolean getDefaultValue() {
		return settings.getSuggestionScrollingDelay() > 0;
	}
}
