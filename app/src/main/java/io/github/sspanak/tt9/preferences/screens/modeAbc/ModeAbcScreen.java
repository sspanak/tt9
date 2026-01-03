package io.github.sspanak.tt9.preferences.screens.modeAbc;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class ModeAbcScreen extends BaseScreenFragment {
	public static final String NAME = "ModeAbc";

	public ModeAbcScreen() { super(); }
	public ModeAbcScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_abc_mode; }
	@Override protected int getXml() { return R.xml.prefs_screen_mode_abc; }

	@Override
	protected void onCreate() {
		resetFontSize(false);
		Preference dropDown = findPreference(DropDownAbcAutoAcceptTime.NAME);
		if (activity != null && dropDown instanceof DropDownAbcAutoAcceptTime) {
			((DropDownAbcAutoAcceptTime) dropDown).populate(activity.getSettings()).preview();
		}
	}
}

