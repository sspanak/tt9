package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.TextTools;

class MindReaderContext {
	@Nullable Language language;

	@Nullable private String lastAppendedWord = null;
	private final int maxTokens;
	@NonNull private final StringBuilder raw = new StringBuilder();
	@NonNull private String[] tokens = new String[0];
	@Nullable private MindReaderNgram[] endingNgrams = null;


	MindReaderContext(int maxTokens) {
		this.maxTokens = Math.max(maxTokens, 0);
	}


	void setLanguage(@NonNull Language language) {
		this.language = language;
	}


	/**
	 * Get all possible ending n-grams for the current context text, starting with the longest one.
	 * The n-grams are generated from the tokens produced by the tokenize() method.
	 */
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


	/**
	 * Appends the given word to the current context text, separating it with a space if needed.
	 * The word is trimmed before appending.
	 */
	boolean appendText(@Nullable String lastWord, boolean addWordSeparator) {
		if (lastWord == null || lastWord.isEmpty()) {
			return false;
		}

		if (addWordSeparator && raw.length() > 0) {
			raw.appendCodePoint(ContextTokenizer.WORD_SEPARATOR);
		}
		raw.append(language == null || language.hasSpaceBetweenWords() ? lastWord.trim() : lastWord);
		lastAppendedWord = lastWord;
		tokens = new String[0];
		endingNgrams = null;

		return true;
	}


	/**
	 * Set new context text, replacing the old one.
	 */
	boolean setText(@NonNull String beforeCursor) {
		if (raw.length() == 0 && beforeCursor.isEmpty()) {
			return false;
		}

		raw.setLength(0);
		raw.append(beforeCursor.trim());
		lastAppendedWord = null;
		tokens = new String[0];
		endingNgrams = null;

		return true;
	}


	/**
	 * Determines whether the current context should be saved to the database.
	 */
	boolean shouldSave(@Nullable InputMode inputMode) {
		if (!InputModeKind.isABC(inputMode)) {
			return true;
		}

		if (!TextTools.isSingleCodePoint(lastAppendedWord)) {
			return false;
		}

		final int cp = lastAppendedWord.codePointAt(0);
		return Character.isWhitespace(cp) || ContextTokenizer.isPunctuationChar(cp);
	}


	/**
	 * Splits the raw text into valid tokens and returns them as an array. Validation is performed by
	 * checking the factory words in the database.
	 */
	@NonNull
	String[] tokenize(@NonNull MindReaderDictionary dictionary) {
		tokens = ContextTokenizer.tokenize(language, raw.toString(), maxTokens);
		filterInvalidTokens(dictionary);

		return tokens;
	}


	/**
	 * Checks which tokens are available as words in the database and returns an array of valid tokens
	 * only. The invalid ones are replaced with MindReaderDictionary.GARBAGE.
	 */
	private void filterInvalidTokens(@NonNull MindReaderDictionary dictionary) {
		if (language == null) {
			return;
		}

		for (int i = 0; i < tokens.length; i++) {
			try {
				if (MindReaderDictionary.isSpecialChar(tokens[i]) || dictionary.contains(tokens[i])) {
					continue;
				}

				final String digitSequence = language.getDigitSequenceForWord(tokens[i]);

				if (tokens[i].contains("'") || tokens[i].contains("-")) {
					// If a word has a valid digit sequence, then it belongs to the language. However, ones
					// with apostrophes or hyphens, like "what's" or "mother-in-law",  are not in the
					// database, so there is no point in running queries for them. We just let them pass,
					// because they are usually useful. Some garbage will go in, but it is what it is.
					continue;
				}

				if (!DataStore.exists(language, tokens[i], digitSequence)) {
					tokens[i] = MindReaderDictionary.GARBAGE;
				}
			} catch (Exception e) {
				if (!(e instanceof InvalidLanguageCharactersException)) {
					Logger.w(getClass().getSimpleName(), "Failed to filter token \"" + tokens[i] + "\" for language " + language.getName() + ": " + e.getMessage());
				}
				tokens[i] = MindReaderDictionary.GARBAGE;
			}
		}
	}


	@Override
	@NonNull
	public String toString() {
		return "raw=\"" + raw + "\", tokens=" + Arrays.toString(tokens);
	}
}
