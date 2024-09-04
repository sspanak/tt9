package io.github.sspanak.tt9.db.wordPairs;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

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
	private final HashMap<Integer, LinkedList<WordPair>> pairs = new HashMap<>();

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
			pairs.put(language.getId(), new LinkedList<>());
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
		if (pairs.get(language.getId()) != null) {
			pairs.get(language.getId()).addFirst(new WordPair(language, word1, word2));
		}
	}


	private void addMiddle(Language language, String word1, String word2) {
		if (pairs.get(language.getId()) != null) {
			int middleIndex = Math.min(pairs.get(language.getId()).size() / 2, MIDDLE_PAIR);
			pairs.get(language.getId()).add(middleIndex, new WordPair(language, word1, word2));
		}
	}


	private void removeExcess(Language language, int index) {
		if (pairs.get(language.getId()) == null || (index == SettingsStore.WORD_PAIR_MAX && pairs.get(language.getId()).size() <= SettingsStore.WORD_PAIR_MAX)) {
			return;
		}

		if (index == SettingsStore.WORD_PAIR_MAX) {
			pairs.get(language.getId()).removeLast();
		} else {
			pairs.get(language.getId()).remove(index);
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
		return indexOf(language, word1, word2) != -1;
	}


	private int indexOf(Language language, String word1, String word2) {
		String SEARCH_TIMER_NAME = "word_pair_search";
		Timer.start(SEARCH_TIMER_NAME);

		if (isInvalid(language, word1, word2)) {
			slowestSearchTime = Math.max(slowestSearchTime, Timer.stop(SEARCH_TIMER_NAME));
			return -1;
		}

		for (int i = 0; pairs.get(language.getId()) != null && i < pairs.get(language.getId()).size(); i++) {
			if (pairs.get(language.getId()).get(i).equals(language, word1, word2)) {
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
			if (pairs.get(language.getId()) != null && !pairs.get(language.getId()).isEmpty()) {
				Logger.d(getClass().getSimpleName(), "Pairs for language: " + language.getId() + " loaded. Nothing to do.");
			}

			ArrayList<WordPair> dbPairs = new ReadOps().getWordPairs(sqlite.getDb(), language);

			for (WordPair pair : dbPairs) {
				add(language, pair.getWord1(), pair.getWord2());
				totalPairs++;
			}
		}

		long currentTime = Timer.stop(LOAD_TIMER_NAME);
		Logger.d(getClass().getSimpleName(), "Loaded " + totalPairs + " word pairs in " + currentTime + " ms");
		slowestLoadTime = Math.max(slowestLoadTime, currentTime);
	}


	@NonNull
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		try {
			for (int langId : pairs.keySet()) {
				sb.append("Language ").append(langId).append(" pairs: ");

				LinkedList<WordPair> langPairs = pairs.get(langId);
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
