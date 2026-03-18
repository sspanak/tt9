package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

class MindReaderContext {
	private final int MAX_TOKENS;

	@Nullable Language language;
	@Nullable private String lastAppendedWord = null;
	@NonNull private final StringBuilder raw = new StringBuilder();
	@NonNull private String[] tokens = new String[0];


	MindReaderContext(int maxTokens) {
		MAX_TOKENS = Math.max(maxTokens, 0);
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

		return true;
	}



	boolean endsWithPunctuation() {
		return tokens.length > 0 && MindReaderDictionary.isSpecialChar(language, tokens[tokens.length - 1]);
	}


	/**
	 * Check which tokens exists as factory words in the database. Non-existing ones are
	 * replaced with MindReaderDictionary.GARBAGE. Existing ones are corrected to the text case in
	 * the database.
	 */
	private void rectifyTokens(@NonNull MindReaderDictionary dictionary) {
		if (language == null) {
			return;
		}

		for (int i = 0; i < tokens.length; i++) {
			try {
				if (language.isTranscribed() || MindReaderDictionary.isSpecialChar(language, tokens[i]) || dictionary.contains(tokens[i])) {
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

				final String word = DataStore.getWord(language, tokens[i], digitSequence);
				if (word == null || word.isEmpty()) {
					tokens[i] = MindReaderDictionary.GARBAGE;
				} else {
					tokens[i] = word; // correct the text case
				}
			} catch (Exception e) {
				if (!(e instanceof InvalidLanguageCharactersException)) {
					Logger.w(getClass().getSimpleName(), "Failed to filter token \"" + tokens[i] + "\" for language " + language.getName() + ": " + e.getMessage());
				}
				tokens[i] = MindReaderDictionary.GARBAGE;
			}
		}
	}


	/**
	 * Convert the current tokens (produced by tokenize()) into an N-gram.
	 */
	@NonNull
	MindReaderNgram toNgram(@NonNull MindReaderDictionary dictionary) {
		return new MindReaderNgram(language, dictionary.getIndices(tokens));
	}


	/**
	 * Similar to toNgram(), but the resulting N-gram will be truncated to maximum context length - 1.
	 * This way, comparing "endingNgram.complete" to "before" of all other N-grams will give us the next
	 * token candidates ("next" of the other N-grams).
	 */
	@NonNull
	MindReaderNgram toEndingNgram(@NonNull MindReaderDictionary dictionary) {
		if (tokens.length == 0) {
			return new MindReaderNgram(language, new int[0]);
		}

		final int[] dictionaryIds =  dictionary.getIndices(tokens);
		final int nGramSize = Math.max(1, Math.min(tokens.length, MAX_TOKENS - 1));
		final int[] ngramTokens = new int[nGramSize];

		System.arraycopy(dictionaryIds, dictionaryIds.length - nGramSize, ngramTokens, 0, nGramSize);
		return new MindReaderNgram(language, ngramTokens);
	}


	boolean isEmpty() {
		return raw.length() == 0;
	}


	void setLanguage(@NonNull Language language) {
		this.language = language;
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

		return true;
	}


	/**
	 * Determines whether the current context should be saved to the database.
	 */
	boolean shouldAutoSave(@Nullable InputMode inputMode) {
		if (isEmpty()) {
			return false;
		}

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
		tokens = ContextTokenizer.tokenize(language, raw.toString(), MAX_TOKENS);
		rectifyTokens(dictionary);

		return tokens;
	}


	@Override
	@NonNull
	public String toString() {
		return "raw=\"" + raw + "\", tokens=" + Arrays.toString(tokens);
	}


	/**
	 * If start of text, simulate we are at the end of a dummy sentence. This makes the guessing
	 * machinery work for the first word in a text field. Also, if the text contains new lines,
	 * ignore everything before the last new line, because it is not relevant for guessing the next
	 * word.
	 */
	static String[] handleStartOfSentenceInSurroundingText(@NonNull Language language, @NonNull String[] surroundingText) {
		final String[] adjusted = new String[] { surroundingText[0], surroundingText[1] };

		final int lastNewLineIndex = surroundingText[0].lastIndexOf('\n');
		adjusted[0] = lastNewLineIndex == -1 ? surroundingText[0] : surroundingText[0].substring(lastNewLineIndex + 1);

		if (!LanguageKind.isThai(language) && adjusted[0].length() < SettingsStore.AUTO_ASSISTANCE_BEFORE_TEXT) {
			String endOfDummySentence = Characters.getChar(language, ".");
			endOfDummySentence = endOfDummySentence == null ? "" : endOfDummySentence;
			adjusted[0] = endOfDummySentence + adjusted[0];
		}

		return adjusted;
	}
}
