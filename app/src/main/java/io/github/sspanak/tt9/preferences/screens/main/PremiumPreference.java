package io.github.sspanak.tt9.preferences.screens.main;

import androidx.preference.Preference;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.preferences.screens.premium.PremiumScreen;

public class PremiumPreference extends ItemClickable {
	public static final String NAME = "screen_premium";

	PreferencesActivity activity;

	public PremiumPreference(Preference item, PreferencesActivity activity) {
		super(item);
		this.activity = activity;
	}

	@Override
	protected boolean onClick(Preference p) {
		if (BuildConfig.PREMIUM && !activity.getSettings().getDemoMode()) {
			activity.displayScreen(PremiumScreen.NAME);
		}
		return false;
	}

	PremiumPreference populate() {
		if (item != null) {
			item.setVisible(BuildConfig.PREMIUM);
		}

		return this;
	}
}
