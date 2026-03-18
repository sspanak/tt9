package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStatic;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

class MindReaderDictionary {
	static final String EMOJI = ":)";
	static final String GARBAGE = "∅";
	static final String NUMBER = "\\d";
	static final String SPACE = " ";
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
	private final int capacity;
	private final HashMap<String, Integer> index;
	private int size = 0;
	private final String[] tokens;


	MindReaderDictionary() {
		this(null);
	}


	MindReaderDictionary(@Nullable Language language) {
		this.locale = language == null ? Locale.getDefault() : language.getLocale();
		this.capacity = SettingsStatic.MIND_READER_MAX_DICTIONARY_WORDS;
		this.index = new HashMap<>(capacity);
		this.tokens = new String[capacity];

		addInternal(GARBAGE);
		addInternal(EMOJI);
		addInternal(NUMBER);
		addInternal(SPACE);
		for (int p : PUNCTUATION) {
			addInternal(new String(Character.toChars(p)));
		}
	}


	static boolean isGarbage(int tokenId) { return tokenId == 0; }
	static boolean isEmoji(int tokenId) { return tokenId == 1; }
	static boolean isNumber(int tokenId) { return tokenId == 2; }
	static boolean isPunctuation(@Nullable Language language, int tokenId) {
		return
			tokenId >= 4 && tokenId < 4 + PUNCTUATION.length
			|| (LanguageKind.usesSpaceAsPunctuation(language) && tokenId == 3);
	}
	static boolean isWord(int tokenId) { return tokenId >= 4 + PUNCTUATION.length; }


	static boolean isSpecialChar(@Nullable Language language, @Nullable String token) {
		if (token == null || token.isEmpty()) {
			return false;
		}

		if (token.equals(EMOJI) || token.equals(NUMBER)) {
			return true;
		}

		if (LanguageKind.usesSpaceAsPunctuation(language) && token.equals(SPACE)) {
			return true;
		}

		for (int p : PUNCTUATION) {
			if (token.codePointAt(0) == p && TextTools.isSingleCodePoint(token)) {
				return true;
			}
		}

		return false;
	}


	private void addInternal(@NonNull String token) {
		tokens[size] = token;
		index.put(token.toLowerCase(locale), size);
		size++;
	}


	private void add(@Nullable Language language, @Nullable String token) {
		if (token == null || token.isEmpty() || size >= capacity || GARBAGE.equals(token) || isSpecialChar(language, token)) {
			return;
		}

		// ignore duplicates
		String tokenIndex = token.toLowerCase(locale);
		if (index.containsKey(tokenIndex)) {
			return;
		}

		// If it is a new token, add it to the end. Larger index means more recently used, hence
		// displayed higher in the suggestions list.
		tokens[size] = token;
		index.put(tokenIndex, size);
		size++;
	}


	void addAll(@Nullable Language language, @NonNull String[] tokens) {
		for (String token : tokens) {
			add(language, token);
		}
	}


	boolean contains(@Nullable String token) {
		return token != null && !token.isEmpty() && index.containsKey(token.toLowerCase(locale));
	}


	private int indexOf(@Nullable String token) {
		if (token == null || token.isEmpty()) {
			return -1;
		}

		Integer idx = index.get(token.toLowerCase(locale));
		return idx == null ? -1 : idx;
	}


	@NonNull
	ArrayList<String> getAll(@NonNull Set<Integer> tokenIds, @Nullable String startsWith) {
		final ArrayList<String> results = new ArrayList<>(tokenIds.size());

		final String prefix = startsWith == null ? null : startsWith.toLowerCase(locale);

		for (int tokenId : tokenIds) {
			if (!isWord(tokenId) || tokenId >= size) {
				continue;
			}

			if (prefix == null || tokens[tokenId].toLowerCase(locale).startsWith(prefix)) {
				results.add(tokens[tokenId]);
			}
		}

		return results;
	}


	/**
	 * Similar to index of, but works with an array of tokens. It returns the index of each token in
	 * the array, or 0 (GARBAGE) if the token is not found.
	 */
	int[] getIndices(@NonNull String[] tokens) {
		final int[] indices = new int[tokens.length];
		for (int i = 0; i < tokens.length; i++) {
			final int idx = indexOf(tokens[i]);
			indices[i] = idx == -1 ? 0 : idx;
		}
		return indices;
	}


	int size() {
		return size;
	}


	@NonNull
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("[");
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i] != null || (tokens[i] == null && i < tokens.length - 2 && tokens[i + 1] != null)) {
				str.append(tokens[i]).append(", ");
			}
		}

		str.setLength(str.length() - 2);
		str.append("]");

		return str.toString();
	}
}
