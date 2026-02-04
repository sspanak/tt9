package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

	private String[] tokens = new String[0];
	private final int capacity;


	MindReaderDictionary(int capacity) {
		this(new String[0], capacity);
	}


	MindReaderDictionary(@NonNull String[] tokens, int capacity) {
		init();
		this.capacity = capacity;
		addAll(tokens);
	}


	private void init() {
		tokens = new String[3 + PUNCTUATION.length + tokens.length];
		tokens[0] = GARBAGE;
		tokens[1] = EMOJI;
		tokens[2] = NUMBER;
		for (int i = 0; i < PUNCTUATION.length; i++) {
			tokens[3 + i] = new String(Character.toChars(PUNCTUATION[i]));
		}
	}


	static boolean isGarbage(int tokenId) { return tokenId == 0; }
	static boolean isEmoji(int tokenId) { return tokenId == 1; }
	static boolean isNumber(int tokenId) { return tokenId == 2; }
	static boolean isPunctuation(int tokenId) { return tokenId >= 3 && tokenId < 3 + PUNCTUATION.length; }


	void add(@Nullable String token) {
		if (token == null || token.isEmpty()) {
			return;
		}

		for (String w : tokens) {
			if (token.equals(w)) {
				return;
			}
		}

		String[] newTokens;
		if (tokens.length >= capacity) {
			newTokens = new String[capacity];
			System.arraycopy(tokens, 1, newTokens, 0, capacity - 1);
		} else {
			newTokens = new String[tokens.length + 1];
			System.arraycopy(tokens, 0, newTokens, 0, tokens.length);
		}
		newTokens[tokens.length] = token;
		tokens = newTokens;
	}


	void addAll(@NonNull String[] tokens) {
		for (String token : tokens) {
			add(token);
		}
	}


	public HashSet<String> getAll(Set<Integer> tokenIds) {
		final HashSet<String> results = new HashSet<>();
		for (Integer id : tokenIds) {
			if (id >= 0 && id < tokens.length) {
				results.add(tokens[id]);
			}
		}
		return results;
	}


	int indexOf(@Nullable String token) {
		if (token == null || token.isEmpty()) {
			return -1;
		}

		for (int i = 0; i < tokens.length; i++) {
			if (token.equals(tokens[i])) {
				return i;
			}
		}

		return -1;
	}


	@NonNull
	@Override
	public String toString() {
		return Arrays.toString(tokens);
	}
}
