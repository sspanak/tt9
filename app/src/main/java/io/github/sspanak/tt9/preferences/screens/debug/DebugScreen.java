package io.github.sspanak.tt9.preferences.screens.debug;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemText;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class DebugScreen extends BaseScreenFragment {
	public static final String NAME = "Debug";

	private static final String DEVICE_INFO_CONTAINER = "pref_device_info";

	public DebugScreen() { init(); }
	public DebugScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_debug_options; }
	@Override protected int getXml() { return R.xml.prefs_screen_debug; }

	@Override
	protected void onCreate() {
		(new ItemText(activity, findPreference(DEVICE_INFO_CONTAINER))).populate(new DeviceInfo().toString()).enableClickHandler();
		(new ItemExportLogcat(findPreference(ItemExportLogcat.NAME), activity)).enableClickHandler();
		(new ItemDemoMode(findPreference(ItemDemoMode.NAME), activity)).populate().enableClickHandler();

		resetFontSize(false);
	}
}
