package io.github.sspanak.tt9.preferences.screens.main;

import androidx.preference.Preference;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;

/**
 * No-op preference because the Premium screen is not available in the open-source version.
 * Implemented in the "premium" variant of the app.
 */
public class PremiumPreference extends ItemClickable {
	public static final String NAME = "screen_premium";

	public PremiumPreference(Preference item, PreferencesActivity ignore) { super(item); }
	@Override protected boolean onClick(Preference p) { return false; }
	PremiumPreference populate() { return this; }
}
