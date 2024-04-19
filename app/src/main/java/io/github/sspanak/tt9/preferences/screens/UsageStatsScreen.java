package io.github.sspanak.tt9.preferences.screens;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.SlowQueryStats;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemText;

public class UsageStatsScreen extends BaseScreenFragment {
	final public static String NAME = "UsageStats";
	final private static String RESET_BUTTON = "pref_slow_queries_reset_stats";
	final private static String SUMMARY_CONTAINER = "summary_container";
	private ItemText queryListContainer;

	public UsageStatsScreen() { init(); }
	public UsageStatsScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_usage_stats; }
	@Override protected int getXml() { return R.xml.prefs_screen_usage_stats; }

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
			logsContainer.setSummary(SlowQueryStats.getSummary());
		}
	}

	private void printSlowQueries() {
		if (queryListContainer == null) {
			queryListContainer = new ItemText(activity, findPreference("query_list_container"));
			queryListContainer.enableClickHandler();
		}

		String slowQueries = SlowQueryStats.getList();
		queryListContainer.populate(slowQueries.isEmpty() ? "No slow queries." : slowQueries);
	}
}
