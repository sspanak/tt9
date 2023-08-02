package io.github.sspanak.tt9.preferences.screens;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

public class DebugScreen extends BaseScreenFragment {
	private static final String DEBUG_LOGS_SWITCH = "pref_enable_debug_logs";
	private static final String SYSTEM_LOGS_SWITCH = "pref_enable_system_logs";
	private static final String LOGS_CONTAINER = "debug_logs_container";

	public DebugScreen() { init(); }
	public DebugScreen(PreferencesActivity activity) { init(activity); }

	@Override protected int getTitle() { return R.string.pref_category_debug_options; }
	@Override protected int getXml() { return R.xml.prefs_screen_debug; }

	@Override
	protected void onCreate() {
		initLogMessagesSwitch();
		initSystemLogsSwitch();

		SwitchPreferenceCompat systemLogs = findPreference(SYSTEM_LOGS_SWITCH);
		boolean includeSystemLogs = systemLogs != null && systemLogs.isChecked();
		printLogs(includeSystemLogs);
	}

	private void initLogMessagesSwitch() {
		SwitchPreferenceCompat msgSwitch = findPreference(DEBUG_LOGS_SWITCH);
		if (msgSwitch == null) {
			Logger.w("DebugScreen", "Debug logs switch not found.");
			return;
		}

		msgSwitch.setChecked(Logger.isDebugLevel());
		msgSwitch.setOnPreferenceChangeListener((Preference p, Object newValue) -> {
			if ((boolean) newValue) {
				Logger.setDebugLevel();
			} else {
				Logger.setDefaultLevel();
			}
			return true;
		});
	}

	private void initSystemLogsSwitch() {
		SwitchPreferenceCompat systemLogs = findPreference(SYSTEM_LOGS_SWITCH);
		if (systemLogs == null) {
			Logger.w("DebugScreen", "System logs switch not found.");
			return;
		}

		systemLogs.setOnPreferenceChangeListener((p, newValue) -> {
			printLogs((boolean) newValue);
			return true;
		});
	}

	private void printLogs(boolean includeSystemLogs) {
		Preference logsContainer = findPreference(LOGS_CONTAINER);
		if (logsContainer == null) {
			Logger.w("DebugScreen", "Logs container not found. Cannot print logs");
			return;
		}

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

		logsContainer.setSummary(log.toString());
	}
}
