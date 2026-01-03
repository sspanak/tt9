package io.github.sspanak.tt9.preferences.screens;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.db.words.SlowQueryStats;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemText;
import io.github.sspanak.tt9.ui.UI;

public class UsageStatsScreen extends BaseScreenFragment {
	final public static String NAME = "UsageStats";
	final private static String RESET_SLOW_QUERIES_BUTTON = "slow_queries_clear_cache";
	final private static String RESET_WORD_PAIRS_CACHE_BUTTON = "word_pair_clear_cache";
	final private static String RESET_WORD_PAIRS_DB_BUTTON = "word_pair_clear_db";

	final private static String SLOW_QUERY_STATS_CONTAINER = "summary_container";
	final private static String WORD_PAIRS_CONTAINER = "word_pairs_container";
	private ItemText queryListContainer;

	public UsageStatsScreen() { super(); }
	public UsageStatsScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_usage_stats; }
	@Override protected int getXml() { return R.xml.prefs_screen_usage_stats; }

	@Override
	protected void onCreate() {
		print(SLOW_QUERY_STATS_CONTAINER, SlowQueryStats.getSummary());
		print(WORD_PAIRS_CONTAINER, DataStore.getWordPairStats());
		printSlowQueries();

		Preference slowQueriesButton = findPreference(RESET_SLOW_QUERIES_BUTTON);
		if (slowQueriesButton != null) {
			slowQueriesButton.setOnPreferenceClickListener(this::resetSlowQueries);
		}

		Preference wordPairsCacheButton = findPreference(RESET_WORD_PAIRS_CACHE_BUTTON);
		if (wordPairsCacheButton != null) {
			wordPairsCacheButton.setOnPreferenceClickListener(this::resetWordPairsCache);
		}

		Preference wordPairsDbButton = findPreference(RESET_WORD_PAIRS_DB_BUTTON);
		if (wordPairsDbButton != null) {
			wordPairsDbButton.setOnPreferenceClickListener(this::deleteWordPairs);
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

	private boolean resetSlowQueries(Preference ignored) {
		SlowQueryStats.clear();
		print(SLOW_QUERY_STATS_CONTAINER, SlowQueryStats.getSummary());
		printSlowQueries();
		return true;
	}

	private boolean resetWordPairsCache(Preference ignored) {
		DataStore.clearWordPairCache();
		print(WORD_PAIRS_CONTAINER, DataStore.getWordPairStats());
		return true;
	}

	private boolean deleteWordPairs(Preference ignored) {
		DataStore.deleteWordPairs(
			LanguageCollection.getAll(),
			() -> UI.toastLongFromAsync(activity, "Word pairs deleted. You must reopen the screen manually.")
		);
		return true;
	}

}
