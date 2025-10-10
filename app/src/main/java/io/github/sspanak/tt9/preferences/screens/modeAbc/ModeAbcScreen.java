package io.github.sspanak.tt9.preferences.screens.modeAbc;

import androidx.preference.Preference;

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
		resetFontSize(false);
		Preference dropDown = findPreference(DropDownAbcAutoAcceptTime.NAME);
		if (dropDown instanceof DropDownAbcAutoAcceptTime) {
			((DropDownAbcAutoAcceptTime) dropDown).populate(activity.getSettings()).preview();
		}
	}
}

