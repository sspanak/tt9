package io.github.sspanak.tt9.preferences.screens;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.SlowQueryStats;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemText;

public class UsageStatsScreen extends BaseScreenFragment {
	final public static String NAME = "UsageStats";
	final private static String RESET_BUTTON = "pref_slow_queries_reset_stats";
	final private static String SLOW_QUERY_STATS_CONTAINER = "summary_container";
	final private static String WORD_PAIRS_CONTAINER = "word_pairs_container";
	private ItemText queryListContainer;

	public UsageStatsScreen() { init(); }
	public UsageStatsScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_usage_stats; }
	@Override protected int getXml() { return R.xml.prefs_screen_usage_stats; }

	@Override
	protected void onCreate() {
		print(SLOW_QUERY_STATS_CONTAINER, SlowQueryStats.getSummary());
		print(WORD_PAIRS_CONTAINER, WordStoreAsync.getPairStats());
		printSlowQueries();

		Preference resetButton = findPreference(RESET_BUTTON);
		if (resetButton != null) {
			resetButton.setOnPreferenceClickListener((Preference p) -> {
				SlowQueryStats.clear();
				WordStoreAsync.clearPairStats();

				print(SLOW_QUERY_STATS_CONTAINER, SlowQueryStats.getSummary());
				print(WORD_PAIRS_CONTAINER, WordStoreAsync.getPairStats());
				printSlowQueries();
				return true;
			});
		}

		resetFontSize(false);
	}

	private void print(String containerName, String text) {
		Preference container = findPreference(containerName);
		if (container != null) {
			container.setSummary(text);
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
