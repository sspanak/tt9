package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;

class MindReaderContext {
	@Nullable Language language;

	private final int maxTokens;
	@NonNull private String raw = "";
	@NonNull private String[] tokens = new String[0];
	@Nullable private MindReaderNgram[] endingNgrams = null;


	MindReaderContext(int maxTokens) {
		this.maxTokens = Math.max(maxTokens, 0);
	}


	void setLanguage(@NonNull Language language) {
		this.language = language;
	}


	@NonNull
	MindReaderNgram[] getEndingNgrams(@NonNull MindReaderDictionary dictionary) {
		if (endingNgrams != null) {
			return endingNgrams;
		}

		final int[] dictionaryIds =  dictionary.indexOf(tokens);
		final int nGramsCount = Math.min(maxTokens, dictionaryIds.length);

		endingNgrams = new MindReaderNgram[nGramsCount];

		for (int i = 0; i < nGramsCount; i++) {
			final int ngramSize = i + 1;
			final int[] ngramTokens = new int[ngramSize];
			System.arraycopy(dictionaryIds, dictionaryIds.length - ngramSize, ngramTokens, 0, ngramSize);
			endingNgrams[i] = new MindReaderNgram(ngramTokens);
		}

		return endingNgrams;
	}


	boolean appendText(@Nullable String lastWord, boolean addTrailingSpace) {
		if (lastWord == null || lastWord.isEmpty()) {
			return false;
		}

		if (addTrailingSpace && !raw.isEmpty()) {
			raw += " ";
		}
		raw += lastWord.trim();
		tokens = new String[0];
		endingNgrams = null;

		return true;
	}


	boolean setText(@NonNull String beforeCursor) {
		if (raw.isEmpty() && beforeCursor.isEmpty()) {
			return false;
		}

		raw = beforeCursor.trim();
		tokens = new String[0];
		endingNgrams = null;

		return true;
	}



	@NonNull
	String[] tokenize() {
		final boolean isLanguageHebrew = LanguageKind.isHebrew(language);
		final boolean allowApostrophesInWords = LanguageKind.isUkrainian(language) || isLanguageHebrew;
		tokens = filterInvalidTokens(
			ContextTokenizer.tokenize(raw, maxTokens, allowApostrophesInWords, isLanguageHebrew)
		);

		return tokens;
	}


	private String[] filterInvalidTokens(@NonNull String[] tokens) {
		// @todo: filter using the database
		return tokens;
	}


	@Override
	@NonNull
	public String toString() {
		return "raw=\"" + raw + "\", tokens=" + Arrays.toString(tokens);
	}
}
