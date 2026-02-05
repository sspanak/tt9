package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;

class MindReaderContext {
	@Nullable Language language;
	@NonNull MindReaderDictionary dictionary;

	private final int maxTokens;
	@NonNull private String rawContext;
	@Nullable private String rawContextEndingWord;
	@NonNull private int[] tokenContext = new int[0];


	MindReaderContext(@NonNull MindReaderDictionary dictionary, int maxTokens) {
		this.dictionary = dictionary;
		this.maxTokens = maxTokens;
		rawContext = "";
	}


	void setLanguage(@NonNull Language language, @NonNull MindReaderDictionary dictionary) {
		this.dictionary = dictionary;
		this.language = language;
	}


	MindReaderNgram[] getEndingNgrams() {
		final int nGramsCount = Math.max(0, tokenContext.length - 1);
		final MindReaderNgram[] ngrams = new MindReaderNgram[nGramsCount];

		for (int i = nGramsCount - 1, j = 0; i >= 0; i--) {
			final int ngramSize = tokenContext.length - i;
			if (ngramSize < 2 || ngramSize > maxTokens) {
				continue;
			}

			final int[] ngramTokens = new int[ngramSize];
			System.arraycopy(tokenContext, i, ngramTokens, 0, ngramSize);
			ngrams[j++] = new MindReaderNgram(ngramTokens);
		}

		return ngrams;
	}


	boolean setText(@NonNull String beforeCursor, @Nullable String endingWord) {
		if (rawContext.isEmpty() && beforeCursor.isEmpty()) {
			return false;
		}

		rawContext = beforeCursor.trim();
		rawContextEndingWord = endingWord != null ? endingWord.trim() : null;

		return true;
	}


	void parseText() {
		final String[] newTokens = filterUnpopularTokens(tokenize());
		dictionary.addAll(newTokens);
		setTokenContext(newTokens);
	}


	private String[] filterUnpopularTokens(String[] tokens) {
		// @todo: filter using the database
		return tokens;
	}


	private void setTokenContext(String[] newTokens) {
		tokenContext = new int[newTokens.length];
		for (int i = 0; i < newTokens.length; i++) {
			tokenContext[i] = dictionary.indexOf(newTokens[i]);
		}
	}


	private String[] tokenize() {
		final boolean isLanguageHebrew = LanguageKind.isHebrew(language);
		return ContextTokenizer.tokenize(
			rawContext,
			rawContextEndingWord,
			maxTokens,
			LanguageKind.isUkrainian(language) || isLanguageHebrew,
			isLanguageHebrew
		);
	}


	@Override
	@NonNull
	public String toString() {
		return "raw=\"" + rawContext + "\", tokens=" + Arrays.toString(tokenContext) + " in dict=" + dictionary;
	}
}
