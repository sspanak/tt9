package io.github.sspanak.tt9.db.wordPairs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.sspanak.tt9.db.sqlite.DeleteOps;
import io.github.sspanak.tt9.db.sqlite.InsertOps;
import io.github.sspanak.tt9.db.sqlite.ReadOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;

public class WordPairStore {
	private final int MIDDLE_PAIR = SettingsStore.WORD_PAIR_MAX / 2;

	// data
	private final SQLiteOpener sqlite;
	private final ConcurrentHashMap<Integer, HashMap<WordPair, WordPair>> pairs = new ConcurrentHashMap<>();

	// timing
	private long slowestAddTime = 0;
	private long slowestLoadTime = 0;
	private long slowestSaveTime = 0;
	private long slowestSearchTime = 0;


	public WordPairStore(Context context) {
		sqlite = SQLiteOpener.getInstance(context);
	}


	public void add(Language language, String word1, String word2, String sequence2) {
		String ADD_TIMER_NAME = "word_pair_add";
		Timer.start(ADD_TIMER_NAME);

		WordPair pair = new WordPair(language, word1, word2, sequence2);
		if (pair.isInvalid()) {
			return;
		}

		HashMap<WordPair, WordPair> languagePairs = pairs.get(language.getId());
		if (languagePairs == null) {
			languagePairs = new HashMap<>();
			pairs.put(language.getId(), languagePairs);
		}

		if (languagePairs.size() >= SettingsStore.WORD_PAIR_MAX) {
			languagePairs.remove(languagePairs.keySet().iterator().next());
		}

		languagePairs.put(pair, pair);

		slowestAddTime = Math.max(slowestAddTime, Timer.stop(ADD_TIMER_NAME));
	}


	public void clear() {
		pairs.clear();
		slowestAddTime = 0;
		slowestSearchTime = 0;
		slowestSaveTime = 0;
		slowestLoadTime = 0;
	}


	@Nullable
	public String getWord2(Language language, String word1, String sequence2) {
		String SEARCH_TIMER_NAME = "word_pair_search";
		Timer.start(SEARCH_TIMER_NAME);

		HashMap<WordPair, WordPair> languagePairs = pairs.get(language.getId());

		if (languagePairs == null) {
			slowestSearchTime = Math.max(slowestSearchTime, Timer.stop(SEARCH_TIMER_NAME));
			return null;
		}

		WordPair pair = languagePairs.get(new WordPair(language, word1, null, sequence2));
		String word2 = pair == null || pair.getWord2().isEmpty() ? null : pair.getWord2();

		slowestSearchTime = Math.max(slowestSearchTime, Timer.stop(SEARCH_TIMER_NAME));
		return word2;
	}


	public void save() {
		String SAVE_TIMER_NAME = "word_pair_save";
		Timer.start(SAVE_TIMER_NAME);

		for (Map.Entry<Integer, HashMap<WordPair, WordPair>> entry : pairs.entrySet()) {
			int langId = entry.getKey();
			HashMap<WordPair, WordPair> languagePairs = entry.getValue();

			ArrayList<WordPair> pairsToSave = new ArrayList<>(languagePairs.keySet());

			sqlite.beginTransaction();
			DeleteOps.deleteWordPairs(sqlite.getDb(), langId);
			InsertOps.insertWordPairs(sqlite.getDb(), langId, pairsToSave);
			sqlite.finishTransaction();
		}


		long currentTime = Timer.stop(SAVE_TIMER_NAME);
		slowestSaveTime = Math.max(slowestSaveTime, currentTime);
		Logger.d(getClass().getSimpleName(), "Saved all word pairs in: " + currentTime + " ms");
	}


	public void load(@NonNull DictionaryLoader dictionaryLoader, ArrayList<Language> languages) {
		if (dictionaryLoader.isRunning()) {
			Logger.e(getClass().getSimpleName(), "Cannot load word pairs while the DictionaryLoader is working.");
			return;
		}

		if (languages == null) {
			Logger.e(getClass().getSimpleName(), "Cannot load word pairs for NULL language list.");
			return;
		}

		String LOAD_TIMER_NAME = "word_pair_load";
		Timer.start(LOAD_TIMER_NAME);

		int totalPairs = 0;
		for (Language language : languages) {
			HashMap<WordPair, WordPair> wordPairs = pairs.get(language.getId());
			if (wordPairs == null) {
				wordPairs = new HashMap<>();
				pairs.put(language.getId(), wordPairs);
			}

			if (!wordPairs.isEmpty()) {
				continue;
			}

			ArrayList<WordPair> dbPairs = new ReadOps().getWordPairs(sqlite.getDb(), language);
			int end = Math.min(dbPairs.size(), SettingsStore.WORD_PAIR_MAX);
			for (int i = 0; i < end; i++, totalPairs++) {
				wordPairs.put(dbPairs.get(i), dbPairs.get(i));
			}

			Logger.d(getClass().getSimpleName(), "Loaded " + wordPairs.size() + " word pairs for language: " + language.getId());
		}

		long currentTime = Timer.stop(LOAD_TIMER_NAME);
		slowestLoadTime = Math.max(slowestLoadTime, currentTime);
		Logger.d(getClass().getSimpleName(), "Loaded " + totalPairs + " word pairs in " + currentTime + " ms");
	}


	@NonNull
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		try {
			for (Map.Entry<Integer, HashMap<WordPair, WordPair>> entry : pairs.entrySet()) {
				int langId = entry.getKey();
				HashMap<WordPair, WordPair> languagePairs = entry.getValue();

				sb.append("Language ").append(langId).append(" pairs: ");
				sb.append(languagePairs.size()).append("\n");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		sb.append("\nSlowest add one time: ").append(slowestAddTime).append(" ms\n");
		sb.append("Slowest search one time: ").append(slowestSearchTime).append(" ms\n");
		sb.append("Slowest save all time: ").append(slowestSaveTime).append(" ms\n");
		sb.append("Slowest load all time: ").append(slowestLoadTime).append(" ms\n");

		return sb.toString();
	}
}
