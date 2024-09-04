package io.github.sspanak.tt9.preferences.screens.modePredictive;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class ModePredictiveScreen extends BaseScreenFragment {
	public static final String NAME = "ModePredictive";

	public ModePredictiveScreen() { init(); }
	public ModePredictiveScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_predictive_mode; }
	@Override protected int getXml() { return R.xml.prefs_screen_mode_predictive; }

	@Override
	protected void onCreate() {
		new ItemSelectZeroKeyCharacter(findPreference(ItemSelectZeroKeyCharacter.NAME), activity).populate().enableClickHandler().preview();
		resetFontSize(false);
	}
}
