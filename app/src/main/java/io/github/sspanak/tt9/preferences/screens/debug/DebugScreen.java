package io.github.sspanak.tt9.preferences.screens.debug;

import androidx.preference.SwitchPreferenceCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemText;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.util.DeviceInfo;
import io.github.sspanak.tt9.util.Logger;

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
		initSystemLogsSwitch();

		SwitchPreferenceCompat systemLogs = findPreference(SYSTEM_LOGS_SWITCH);
		boolean includeSystemLogs = systemLogs != null && systemLogs.isChecked();
		printLogs(includeSystemLogs);
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
		StringBuilder log = new StringBuilder();
		try {
				Process process = Runtime.getRuntime().exec("logcat -d -v threadtime io.github.sspanak.tt9:D");
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

				String line;
				while ((line = bufferedReader.readLine()) != null) {
					if (includeSystemLogs || line.contains(Logger.TAG_PREFIX)) {
						log.append(line).append("\n\n");
					}
				}
		}
		catch (IOException e) {
			log.append("Error getting the logs. ").append(e.getMessage());
		}

		if (log.toString().isEmpty()) {
			log.append("No Logs");
		}

		if (logsContainer == null) {
			logsContainer = new ItemText(activity, findPreference("debug_logs_container"));
			logsContainer.enableClickHandler();
		}
		logsContainer.populate(log.toString());
	}
}
