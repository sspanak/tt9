package io.github.sspanak.tt9.preferences.screens;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

public class PremiumScreen extends BaseScreenFragment {
	public static final String NAME = "Premium";

	public PremiumScreen() { init(); }
	public PremiumScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_premium; }
	@Override protected int getXml() { return R.xml.prefs_screen_premium; }
	@Override protected void onCreate() {}
}
