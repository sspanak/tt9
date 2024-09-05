package io.github.sspanak.tt9.db.wordPairs;

import android.content.Context;

import androidx.annotation.NonNull;

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
	private final ConcurrentHashMap<Integer, HashMap<WordPair, Integer>> pairs = new ConcurrentHashMap<>();

	// timing
	private long slowestAddTime = 0;
	private long slowestLoadTime = 0;
	private long slowestSaveTime = 0;
	private long slowestSearchTime = 0;


	public WordPairStore(Context context) {
		sqlite = SQLiteOpener.getInstance(context);
	}

	public void add(Language language, String word1, String word2) {
		String ADD_TIMER_NAME = "word_pair_add";
		Timer.start(ADD_TIMER_NAME);

		WordPair pair = new WordPair(language, word1, word2);
		if (pair.isInvalid()) {
			return;
		}

		HashMap<WordPair, Integer> languagePairs = pairs.get(language.getId());
		if (languagePairs == null) {
			languagePairs = new HashMap<>();
			pairs.put(language.getId(), languagePairs);
		}

		Integer index = languagePairs.get(pair);

		if (index == null) {
			removeLast(languagePairs);
			addMiddle(languagePairs, pair);
		} else {
			removePair(languagePairs, index);
			addFirst(languagePairs, pair);
		}

//		Logger.d("WordPairStore", "All pairs: " + languagePairs.keySet());

		slowestAddTime = Math.max(slowestAddTime, Timer.stop(ADD_TIMER_NAME));
	}

	private void addFirst(HashMap<WordPair, Integer> languagePairs, WordPair pair) {
		languagePairs.put(pair, 0);
		shiftIndices(languagePairs, 1);
	}

	private void addMiddle(HashMap<WordPair, Integer> languagePairs, WordPair pair) {
		int middleIndex = Math.min((int) Math.floor(languagePairs.size() / 2.0), MIDDLE_PAIR);
		languagePairs.put(pair, middleIndex);
		shiftIndices(languagePairs, middleIndex + 1);
	}

	private void removeLast(HashMap<WordPair, Integer> languagePairs) {
		if (languagePairs.size() > SettingsStore.WORD_PAIR_MAX) {
			WordPair lastPair = null;
			int lastIndex = -1;
			for (Map.Entry<WordPair, Integer> entry : languagePairs.entrySet()) {
				if (entry.getValue() > lastIndex) {
					lastIndex = entry.getValue();
					lastPair = entry.getKey();
				}
			}
			if (lastPair != null) {
				languagePairs.remove(lastPair);
			}
		}
	}

	private void removePair(HashMap<WordPair, Integer> languagePairs, int index) {
		shiftIndices(languagePairs, index, -1);
	}

	private void shiftIndices(HashMap<WordPair, Integer> languagePairs, int startIndex) {
		shiftIndices(languagePairs, startIndex, 1);
	}

	private void shiftIndices(HashMap<WordPair, Integer> languagePairs, int startIndex, int increment) {
		for (Map.Entry<WordPair, Integer> entry : languagePairs.entrySet()) {
			if (entry.getValue() >= startIndex) {
				languagePairs.put(entry.getKey(), entry.getValue() + increment);
			}
		}
	}

	public void clear() {
		pairs.clear();
		slowestAddTime = 0;
		slowestSearchTime = 0;
		slowestSaveTime = 0;
		slowestLoadTime = 0;
	}

	public boolean contains(Language language, String word1, String word2) {
		String SEARCH_TIMER_NAME = "word_pair_search";
		Timer.start(SEARCH_TIMER_NAME);

		HashMap<WordPair, Integer> languagePairs = pairs.get(language.getId());
		WordPair pair = new WordPair(language, word1, word2);

		if (languagePairs == null || pair.isInvalid()) {
			slowestSearchTime = Math.max(slowestSearchTime, Timer.stop(SEARCH_TIMER_NAME));
			return false;
		}

		boolean pairExists = languagePairs.containsKey(pair);

//		Logger.d("WordPairStore", "Pair " + pair + " exists: " + pairExists + " is valid: " + !pair.isInvalid());

		slowestSearchTime = Math.max(slowestSearchTime, Timer.stop(SEARCH_TIMER_NAME));
		return pairExists;
	}

	public void save() {

		String SAVE_TIMER_NAME = "word_pair_save";
		Timer.start(SAVE_TIMER_NAME);

		for (Map.Entry<Integer, HashMap<WordPair, Integer>> entry : pairs.entrySet()) {
			int langId = entry.getKey();
			HashMap<WordPair, Integer> languagePairs = entry.getValue();

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
			HashMap<WordPair, Integer> wordPairs = pairs.get(language.getId());
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
				wordPairs.put(dbPairs.get(i), i);
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
			for (Map.Entry<Integer, HashMap<WordPair, Integer>> entry : pairs.entrySet()) {
				int langId = entry.getKey();
				HashMap<WordPair, Integer> languagePairs = entry.getValue();

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
