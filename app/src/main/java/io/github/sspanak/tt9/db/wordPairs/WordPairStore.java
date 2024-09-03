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
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.Timer;

public class WordPairStore {
	private final int MAX_WORD_LENGTH = 5;
	private final int MAX_PAIRS = 1000;
	private final int MIDDLE_PAIR = MAX_PAIRS / 2;

	private final SQLiteOpener sqlite;

	private final HashMap<Integer, LinkedList<WordPair>> pairs = new HashMap<>();

	private final String ADD_TIMER_NAME = "word_pair_add";
	private final String SEARCH_TIMER_NAME = "word_pair_search";
	private final String LOAD_TIMER_NAME = "word_pair_load";
	private final String SAVE_TIMER_NAME = "word_pair_save";

	private long slowestAddTime = 0;
	private long slowestSearchTime = 0;
	private long slowestLoadTime = 0;
	private long slowestSaveTime = 0;

	public WordPairStore(Context context) {
		sqlite = SQLiteOpener.getInstance(context);
	}


	public boolean isInvalid(Language language, String word1, String word2) {
		return
			language == null
			|| word1 == null || word2 == null
			|| word1.isEmpty() && word2.isEmpty()
			|| word1.length() > MAX_WORD_LENGTH || word2.length() > MAX_WORD_LENGTH
			|| word1.equals(word2)
			|| !(new Text(word1).isAlphabetic()) || !(new Text(word2).isAlphabetic());
	}


	public void add(Language language, String word1, String word2) {
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
			removeExcess(language, MAX_PAIRS);
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
			int middleIndex = Math.min(pairs.get(language.getId()).size(), MIDDLE_PAIR);
			pairs.get(language.getId()).add(middleIndex, new WordPair(language, word1, word2));
		}
	}


	private void removeExcess(Language language, int index) {
		if (pairs.get(language.getId()) == null || (index == MAX_PAIRS && pairs.get(language.getId()).size() <= MAX_PAIRS)) {
			return;
		}

		if (index == MAX_PAIRS) {
			pairs.get(language.getId()).removeLast();
		} else {
			pairs.get(language.getId()).remove(index);
		}
	}


	public void clear() {
		pairs.clear();
		slowestAddTime = 0;
		slowestSearchTime = 0;
	}


	private void clear(int langId) {
		if (pairs.get(langId) != null) {
			pairs.get(langId).clear();
		}
	}


	public boolean contains(Language language, String word1, String word2) {
		return indexOf(language, word1, word2) != -1;
	}


	private int indexOf(Language language, String word1, String word2) {
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
		Timer.start(SAVE_TIMER_NAME);

		for (int langId : pairs.keySet()) {
			sqlite.beginTransaction();
			DeleteOps.deleteWordPairs(sqlite.getDb(), langId);
			InsertOps.insertWordPairs(sqlite.getDb(), langId, pairs.get(langId));
			sqlite.finishTransaction();
		}

		slowestSaveTime = Math.max(slowestSaveTime, Timer.stop(SAVE_TIMER_NAME));
	}


	public void load(ArrayList<Language> languages) {
		if (!pairs.isEmpty()) {
			Logger.d(getClass().getSimpleName(), "Pairs already loaded. Nothing to do.");
		}

		if (languages == null) {
			Logger.e(getClass().getSimpleName(), "Cannot load word pairs for NULL language list.");
			return;
		}

		Timer.start(LOAD_TIMER_NAME);

		int totalPairs = 0;
		for (Language language : languages) {
			clear(language.getId());
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
				sb.append("Language ").append(langId).append(" total: ");

				LinkedList<WordPair> langPairs = pairs.get(langId);
				sb.append(langPairs == null ? "0" : langPairs.size()).append("\n");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		sb.append("Slowest add time: ").append(slowestAddTime).append(" ms\n");
		sb.append("Slowest search time: ").append(slowestSearchTime).append(" ms\n");
		sb.append("Slowest save time: ").append(slowestSaveTime).append(" ms\n");
		sb.append("Slowest load time: ").append(slowestLoadTime).append(" ms\n");

		return sb.toString();
	}
}
