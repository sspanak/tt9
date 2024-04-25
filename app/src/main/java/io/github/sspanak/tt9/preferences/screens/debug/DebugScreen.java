package io.github.sspanak.tt9.preferences.screens.debug;

import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.exporter.LogcatExporter;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemText;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class DebugScreen extends BaseScreenFragment {
	public static final String NAME = "Debug";

	private static final String DEVICE_INFO_CONTAINER = "pref_device_info";
	private static final String SYSTEM_LOGS_SWITCH = "pref_enable_system_logs";


	private ItemText logsContainer;

	public DebugScreen() { init(); }
	public DebugScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_debug_options; }
	@Override protected int getXml() { return R.xml.prefs_screen_debug; }

	@Override
	protected void onCreate() {
		(new ItemLogLevel(findPreference(ItemLogLevel.NAME))).populate().preview().enableClickHandler();
		(new ItemInputHandlingMode(findPreference(ItemInputHandlingMode.NAME), activity.getSettings())).populate().preview().enableClickHandler();
		(new ItemText(activity, findPreference(DEVICE_INFO_CONTAINER))).populate(new DeviceInfo().toString()).enableClickHandler();
		(new ItemExportLogcat(findPreference(ItemExportLogcat.NAME), activity)).enableClickHandler();
		initSystemLogsSwitch();

		SwitchPreferenceCompat systemLogs = findPreference(SYSTEM_LOGS_SWITCH);
		boolean includeSystemLogs = systemLogs != null && systemLogs.isChecked();
		printLogs(includeSystemLogs);

		resetFontSize(false);
	}

	private void initSystemLogsSwitch() {
		SwitchPreferenceCompat systemLogs = findPreference(SYSTEM_LOGS_SWITCH);
		if (systemLogs != null) {
			systemLogs.setOnPreferenceChangeListener((p, newValue) -> {
				printLogs((boolean) newValue);
				return true;
			});
		}
	}

	private void printLogs(boolean includeSystemLogs) {
		if (logsContainer == null) {
			logsContainer = new ItemText(activity, findPreference("debug_logs_container"));
			logsContainer.enableClickHandler();
		}

		String logs = LogcatExporter.getLogs(includeSystemLogs).replace("\n", "\n\n");
		if (logs.isEmpty()) {
			logs = "No Logs";
		}
		logsContainer.populate(logs);
	}
}
