package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.TextTools;
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

	@NonNull private final Locale locale;
	@NonNull private String[] tokens = new String[0];
	private final int capacity;


	MindReaderDictionary(int capacity) {
		this(null, capacity);
	}


	MindReaderDictionary(@Nullable Language language, int capacity) {
		init();
		this.capacity = capacity;
		this.locale = language == null ? Locale.getDefault() : language.getLocale();
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
	static boolean isWord(int tokenId) { return tokenId >= 3 + PUNCTUATION.length; }


	static boolean isSpecialChar(@Nullable String token) {
		if (token == null || token.isEmpty()) {
			return false;
		}

		if (token.equals(EMOJI) || token.equals(NUMBER)) {
			return true;
		}

		for (int p : PUNCTUATION) {
			if (token.codePointAt(0) == p && TextTools.isSingleCodePoint(token)) {
				return true;
			}
		}

		return false;
	}


	private void add(@Nullable String token) {
		if (token == null || token.isEmpty() || GARBAGE.equals(token) || isSpecialChar(token)) {
			return;
		}

		final int tokenIndex = indexOf(token);
		if (tokenIndex != -1) {
			tokens[tokenIndex] = token;
			return;
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


	@NonNull
	public ArrayList<String> getAll(@NonNull Set<Integer> tokenIds, @Nullable String startsWith) {
		final ArrayList<String> results = new ArrayList<>(tokenIds.size());

		for (final int tokenId : tokenIds) {
			if (isWord(tokenId) && tokenId < tokens.length && (startsWith == null || tokens[tokenId].toLowerCase(locale).startsWith(startsWith.toLowerCase(locale)))) {
				results.add(tokens[tokenId]);
			}
		}

		return results;
	}


	int indexOf(@Nullable String token) {
		if (token == null || token.isEmpty()) {
			return -1;
		}

		for (int i = 0; i < tokens.length; i++) {
			if (token.toLowerCase(locale).equals(tokens[i].toLowerCase(locale))) {
				return i;
			}
		}

		return -1;
	}


	int[] indexOf(@NonNull String[] tokens) {
		final int[] indices = new int[tokens.length];
		for (int i = 0; i < tokens.length; i++) {
			indices[i] = indexOf(tokens[i]);
		}
		return indices;
	}


	@NonNull
	@Override
	public String toString() {
		return Arrays.toString(tokens);
	}
}
