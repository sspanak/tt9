package io.github.sspanak.tt9.db.wordPairs;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.github.sspanak.tt9.db.sqlite.DeleteOps;
import io.github.sspanak.tt9.db.sqlite.InsertOps;
import io.github.sspanak.tt9.db.sqlite.ReadOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.Timer;

public class WordPairStore {
	private final int MIDDLE_PAIR = SettingsStore.WORD_PAIR_MAX / 2;

	// data
	private final SQLiteOpener sqlite;
	private final ConcurrentHashMap<Integer, ArrayList<WordPair>> pairs = new ConcurrentHashMap<>();

	// timing
	private long slowestAddTime = 0;
	private long slowestLoadTime = 0;
	private long slowestSaveTime = 0;
	private long slowestSearchTime = 0;


	public WordPairStore(Context context) {
		sqlite = SQLiteOpener.getInstance(context);
	}


	public boolean isInvalid(Language language, String word1, String word2) {
		return
			language == null
			|| word1 == null || word2 == null
			|| word1.isEmpty() || word2.isEmpty()
			|| (word1.length() > SettingsStore.WORD_PAIR_MAX_WORD_LENGTH && word2.length() > SettingsStore.WORD_PAIR_MAX_WORD_LENGTH)
			|| word1.equals(word2)
			|| !(new Text(word1).isAlphabetic()) || !(new Text(word2).isAlphabetic());
	}


	public void add(Language language, String word1, String word2) {
		String ADD_TIMER_NAME = "word_pair_add";
		Timer.start(ADD_TIMER_NAME);

//		Logger.d(getClass().getSimpleName(), "Attempting to add pair: (" + word1 + "," + word2 + ")");

		if (isInvalid(language, word1, word2)) {
			Logger.d(getClass().getSimpleName(), "Ignoring invalid pair: (" + word1 + "," + word2 + ")");
			return;
		}

		if (pairs.get(language.getId()) == null) {
			pairs.put(language.getId(), new ArrayList<>());
		}

		int index = indexOf(language, word1, word2);

		if (index == -1) {
			addMiddle(language, word1, word2);
			removeExcess(language, SettingsStore.WORD_PAIR_MAX);
		} else {
			removeExcess(language, index);
			addFirst(language, word1, word2);
		}

		slowestAddTime = Math.max(slowestAddTime, Timer.stop(ADD_TIMER_NAME));
	}


	private void addFirst(Language language, String word1, String word2) {
		ArrayList<WordPair> languagePairs = pairs.get(language.getId());
		if (languagePairs != null) {
			languagePairs.add(0, new WordPair(language, word1, word2));
		}
	}


	private void addMiddle(Language language, String word1, String word2) {
		ArrayList<WordPair> languagePairs = pairs.get(language.getId());
		if (languagePairs != null) {
			int middleIndex = Math.min(languagePairs.size() / 2, MIDDLE_PAIR);
			languagePairs.add(middleIndex, new WordPair(language, word1, word2));
		}
	}


	private void removeExcess(Language language, int index) {
		ArrayList<WordPair> languagePairs = pairs.get(language.getId());
		if (languagePairs == null || (index == SettingsStore.WORD_PAIR_MAX && languagePairs.size() <= SettingsStore.WORD_PAIR_MAX)) {
			return;
		}

		languagePairs.remove(index);
	}


	public void clear() {
		pairs.clear();
		slowestAddTime = 0;
		slowestSearchTime = 0;
		slowestSaveTime = 0;
		slowestLoadTime = 0;
	}

	public boolean contains(Language language, String word1, String word2) {
		return indexOf(language, word1, word2) != -1;
	}


	private int indexOf(Language language, String word1, String word2) {
		String SEARCH_TIMER_NAME = "word_pair_search";
		Timer.start(SEARCH_TIMER_NAME);

		if (isInvalid(language, word1, word2)) {
			slowestSearchTime = Math.max(slowestSearchTime, Timer.stop(SEARCH_TIMER_NAME));
			return -1;
		}

		List<WordPair> languagePairs = pairs.get(language.getId());
		if (languagePairs == null) {
			slowestSearchTime = Math.max(slowestSearchTime, Timer.stop(SEARCH_TIMER_NAME));
			return -1;
		}


		for (int i = 0; i < languagePairs.size(); i++) {
			WordPair pair = languagePairs.get(i);
			if (pair != null && pair.equals(language, word1, word2)) {
				slowestSearchTime = Math.max(slowestSearchTime, Timer.stop(SEARCH_TIMER_NAME));
				return i;
			}
		}

		slowestSearchTime = Math.max(slowestSearchTime, Timer.stop(SEARCH_TIMER_NAME));
		return -1;
	}


	public void save() {
		String SAVE_TIMER_NAME = "word_pair_save";
		Timer.start(SAVE_TIMER_NAME);

		for (int langId : pairs.keySet()) {
			sqlite.beginTransaction();
			DeleteOps.deleteWordPairs(sqlite.getDb(), langId);
			InsertOps.insertWordPairs(sqlite.getDb(), langId, pairs.get(langId));
			sqlite.finishTransaction();
		}

		slowestSaveTime = Math.max(slowestSaveTime, Timer.stop(SAVE_TIMER_NAME));
	}


	public void load(@NonNull DictionaryLoader dictionaryLoader, ArrayList<Language> languages) {
		if (dictionaryLoader.isRunning()) {
			Logger.e(getClass().getSimpleName(), "Cannot load word pairs while dictionary is still loading.");
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
			ArrayList<WordPair> wordPairs = pairs.get(language.getId());
			wordPairs = wordPairs == null ? new ArrayList<>() : wordPairs;

			if (!wordPairs.isEmpty()) {
				continue;
			}

			ArrayList<WordPair> dbPairs = new ReadOps().getWordPairs(sqlite.getDb(), language);
			int end = Math.min(dbPairs.size(), SettingsStore.WORD_PAIR_MAX);
			for (int i = wordPairs.size(); i < end; i++, totalPairs++) {
				wordPairs.add(dbPairs.get(i));
			}

			pairs.put(language.getId(), wordPairs);

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
			for (int langId : pairs.keySet()) {
				sb.append("Language ").append(langId).append(" pairs: ");

				ArrayList<WordPair> langPairs = pairs.get(langId);
				sb.append(langPairs == null ? "0" : langPairs.size()).append("\n");
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
