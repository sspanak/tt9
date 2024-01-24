package io.github.sspanak.tt9.preferences.screens;

import androidx.preference.Preference;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.SlowQueryStats;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

public class SlowQueriesScreen extends BaseScreenFragment {
	private final static String RESET_BUTTON = "pref_slow_queries_reset_stats";
	private final static String SUMMARY_CONTAINER = "summary_container";
	private final static String QUERY_LIST_CONTAINER = "query_list_container";

	public SlowQueriesScreen() { init(); }
	public SlowQueriesScreen(PreferencesActivity activity) { init(activity); }

	@Override protected int getTitle() { return R.string.pref_category_slow_queries; }
	@Override protected int getXml() { return R.xml.prefs_screen_slow_queries; }

	@Override
	protected void onCreate() {
		printSummary();
		printSlowQueries();

		Preference resetButton = findPreference(RESET_BUTTON);
		if (resetButton != null) {
			resetButton.setOnPreferenceClickListener((Preference p) -> {
				SlowQueryStats.clear();
				printSummary();
				printSlowQueries();
				return true;
			});
		}
	}

	private void printSummary() {
		Preference logsContainer = findPreference(SUMMARY_CONTAINER);
		if (logsContainer != null) {
			logsContainer.setSummary(Logger.isDebugLevel() ? SlowQueryStats.getSummary() : "Debugging disabled");
		}
	}

	private void printSlowQueries() {
		Preference queryListContainer = findPreference(QUERY_LIST_CONTAINER);
		if (queryListContainer != null) {
			String message;
			if (!Logger.isDebugLevel()) {
				message = "Debugging disabled";
			} else {
				message = SlowQueryStats.getList();
				message = message.isEmpty() ? "No slow queries." : message;
			}
			queryListContainer.setSummary(message);
		}
	}
}
