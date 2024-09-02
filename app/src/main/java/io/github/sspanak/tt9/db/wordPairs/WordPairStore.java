package io.github.sspanak.tt9.db.wordPairs;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.LinkedList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;

public class WordPairStore {
	private final int MAX_PAIRS = 1000;
	private final int MIDDLE_PAIR = MAX_PAIRS / 2;

	private final int MAX_WORD_LENGTH = 5;

	private final HashMap<Integer, LinkedList<WordPair>> pairs = new HashMap<>();


	public boolean isInvalid(Language language, String word1, String word2) {
		return
			language == null
			|| word1 == null || word2 == null
			|| word1.length() > MAX_WORD_LENGTH || word2.length() > MAX_WORD_LENGTH
			|| word1.equals(word2)
			|| !(new Text(word1).isAlphabetic()) || !(new Text(word2).isAlphabetic());
	}


	public void add(Language language, String word1, String word2) {
		Logger.d(getClass().getSimpleName(), "Attempting to add pair: (" + word1 + "," + word2 + ")");

		if (isInvalid(language, word1, word2)) {
			Logger.d(getClass().getSimpleName(), "Invalid pair: (" + word1 + "," + word2 + ")");
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


	public boolean contains(Language language, String word1, String word2) {
		return indexOf(language, word1, word2) != -1;
	}


	public int indexOf(Language language, String word1, String word2) {
		if (isInvalid(language, word1, word2)) {
			return -1;
		}

		for (int i = 0; pairs.get(language.getId()) != null && i < pairs.get(language.getId()).size(); i++) {
			if (pairs.get(language.getId()).get(i).equals(language, word1, word2)) {
				return i;
			}
		}

		return -1;
	}

	@NonNull
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (int langId : pairs.keySet()) {
			sb.append("language ").append(langId).append(": [");

			LinkedList<WordPair> langPairs = pairs.get(langId);

			if (langPairs != null) {
				for (WordPair pair : langPairs) {
					sb.append(pair).append(",");
				}
			}

			sb.append("]\n");
		}

		return sb.toString();
	}
}
