package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

class MindReaderDictionary {
	static final String NULL_WORD = "∅";
	static final String EMOJI_WORD = ":)";
	static final String NUMBER_WORD = "\\d";

	private String[] words = { NULL_WORD };
	private final int capacity;


	MindReaderDictionary(int capacity) {
		this(new String[0], capacity);
	}


	MindReaderDictionary(@NonNull String[] words, int capacity) {
		this.capacity = capacity;
		addMany(words);
	}


	void add(@Nullable String word) {
		if (word == null || word.isEmpty()) {
			return;
		}

		for (String w : words) {
			if (word.equals(w)) {
				return;
			}
		}

		String[] newWords;
		if (words.length >= capacity) {
			newWords = new String[capacity];
			System.arraycopy(words, 1, newWords, 0, capacity - 1);
		} else {
			newWords = new String[words.length + 1];
			System.arraycopy(words, 0, newWords, 0, words.length);
		}
		newWords[words.length] = word;
		words = newWords;
	}


	void addMany(@NonNull String[] words) {
		for (String word : words) {
			add(word);
		}
	}


	int indexOf(@Nullable String word) {
		if (word == null || word.isEmpty()) {
			return -1;
		}

		for (int i = 0; i < words.length; i++) {
			if (word.equals(words[i])) {
				return i;
			}
		}

		return -1;
	}


	@NonNull
	@Override
	public String toString() {
		return Arrays.toString(words);
	}
}
