package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import io.github.sspanak.tt9.util.chars.Characters;

class MindReaderDictionary {
	static final String EMOJI = ":)";
	static final String GARBAGE = "∅";
	static final String NUMBER = "\\d";
	static final int[] PUNCTUATION = {
		Characters.AR_QUESTION_MARK.codePointAt(0),
		Characters.GR_QUESTION_MARK.codePointAt(0),
		Characters.ZH_QUESTION_MARK.codePointAt(0),
		Characters.ZH_EXCLAMATION_MARK.codePointAt(0),
		Characters.ZH_FULL_STOP.codePointAt(0),
		'!',
		'?',
		',',
		'.'
	};

	private String[] words = new String[0];
	private final int capacity;


	MindReaderDictionary(int capacity) {
		this(new String[0], capacity);
	}


	MindReaderDictionary(@NonNull String[] words, int capacity) {
		init();
		this.capacity = capacity;
		addMany(words);
	}


	private void init() {
		words = new String[3 + PUNCTUATION.length + words.length];
		words[0] = GARBAGE;
		words[1] = EMOJI;
		words[2] = NUMBER;
		for (int i = 0; i < PUNCTUATION.length; i++) {
			words[3 + i] = new String(Character.toChars(PUNCTUATION[i]));
		}
	}


	static boolean isGarbage(int tokenId) { return tokenId == 0; }
	static boolean isEmoji(int tokenId) { return tokenId == 1; }
	static boolean isNumber(int tokenId) { return tokenId == 2; }
	static boolean isPunctuation(int tokenId) { return tokenId >= 3 && tokenId < 3 + PUNCTUATION.length; }


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
