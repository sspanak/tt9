package io.github.sspanak.tt9.preferences.screens.appearance;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class AppearanceScreen extends BaseScreenFragment {
	final public static String NAME = "Appearance";
	public AppearanceScreen() { init(); }
	public AppearanceScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_appearance; }
	@Override protected int getXml() { return R.xml.prefs_screen_appearance; }

	@Override
	protected void onCreate() {
		(new ItemSelectTheme(activity, findPreference(ItemSelectTheme.NAME)))
			.populate()
			.enableClickHandler()
			.preview();

		(new ItemSelectLayoutType(activity, findPreference(ItemSelectLayoutType.NAME)))
			.populate()
			.enableClickHandler()
			.preview();
	}
}
