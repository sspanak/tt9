package io.github.sspanak.tt9.preferences.screens;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.db.mindReading.MindReader;
import io.github.sspanak.tt9.db.words.SlowQueryStats;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemText;
import io.github.sspanak.tt9.ui.UI;

public class UsageStatsScreen extends BaseScreenFragment {
	public final static String NAME = "UsageStats";

	private final static String CONTAINER_SLOW_QUERY_STATS = "summary_container";
	private final static String BUTTON_RESET_SLOW_QUERIES = "slow_queries_clear_cache";

	private final static String CONTAINER_MIND_READER_STATS = "mind_reader_container";
	private final static String BUTTON_RESET_MIND_READER_DB = "mind_reader_clear_db";

	private final static String CONTAINER_WORD_PAIRS = "word_pairs_container";
	private final static String BUTTON_RESET_WORD_PAIRS_CACHE = "word_pair_clear_cache";
	private final static String BUTTON_RESET_WORD_PAIRS_DB = "word_pair_clear_db";


	private ItemText queryListContainer;

	public UsageStatsScreen() { super(); }
	public UsageStatsScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_usage_stats; }
	@Override protected int getXml() { return R.xml.prefs_screen_usage_stats; }

	@Override
	protected void onCreate() {
		print(CONTAINER_MIND_READER_STATS, MindReader.getStats());
		print(CONTAINER_WORD_PAIRS, DataStore.getWordPairStats());
		print(CONTAINER_SLOW_QUERY_STATS, SlowQueryStats.getSummary());
		printSlowQueries();

		Preference slowQueriesButton = findPreference(BUTTON_RESET_SLOW_QUERIES);
		if (slowQueriesButton != null) {
			slowQueriesButton.setOnPreferenceClickListener(this::resetSlowQueries);
		}

		Preference mindReaderDbButton = findPreference(BUTTON_RESET_MIND_READER_DB);
		if (mindReaderDbButton != null) {
			mindReaderDbButton.setOnPreferenceClickListener(this::deleteMindReaderDb);
		}

		Preference wordPairsCacheButton = findPreference(BUTTON_RESET_WORD_PAIRS_CACHE);
		if (wordPairsCacheButton != null) {
			wordPairsCacheButton.setOnPreferenceClickListener(this::resetWordPairsCache);
		}

		Preference wordPairsDbButton = findPreference(BUTTON_RESET_WORD_PAIRS_DB);
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

	private boolean deleteMindReaderDb(Preference ignored) {
		UI.toast(activity, "Not implemented yet.");
		return true;
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
		print(CONTAINER_SLOW_QUERY_STATS, SlowQueryStats.getSummary());
		printSlowQueries();
		return true;
	}

	private boolean resetWordPairsCache(Preference ignored) {
		DataStore.clearWordPairCache();
		print(CONTAINER_WORD_PAIRS, DataStore.getWordPairStats());
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
