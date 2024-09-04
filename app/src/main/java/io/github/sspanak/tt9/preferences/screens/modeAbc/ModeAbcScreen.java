package io.github.sspanak.tt9.preferences.screens.modeAbc;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class ModeAbcScreen extends BaseScreenFragment {
	public static final String NAME = "ModeAbc";

	public ModeAbcScreen() { init(); }
	public ModeAbcScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_abc_mode; }
	@Override protected int getXml() { return R.xml.prefs_screen_mode_abc; }

	@Override
	protected void onCreate() {
		new ItemSelectAbcAutoAccceptTime(findPreference(ItemSelectAbcAutoAccceptTime.NAME), activity).populate().enableClickHandler().preview();
		resetFontSize(false);
	}
}

