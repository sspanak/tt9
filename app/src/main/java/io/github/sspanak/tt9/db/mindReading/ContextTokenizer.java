package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.util.chars.Characters;

class ContextTokenizer {
	private enum TokenType {SPACE, WORD, PUNCTUATION, NUMBER, EMOJI, GARBAGE}

	private static int tokensCount;

	static String[] tokenize(@NonNull String text, @Nullable String endingWord, int maxTokens, boolean allowApostrophe, boolean allowQuote) {
		final StringBuilder current = new StringBuilder();
		final String[] tokens = new String[maxTokens];
		final boolean textContainsEndingWord = endingWord != null && !endingWord.isEmpty() && text.endsWith(endingWord);

		tokensCount = 0;
		TokenType previousType = TokenType.SPACE;

		final int end = textContainsEndingWord ? text.length() - endingWord.length() : text.length();
		for (int i = 0; i < end; ) {
			final int cp = text.codePointAt(i);
			final int step = Character.charCount(cp);

			TokenType type;
			if (Character.isWhitespace(cp)) {
				type = TokenType.SPACE;
			} else if (isWordChar(cp, allowApostrophe, allowQuote)) {
				type = TokenType.WORD;
			} else if (isPunctuationChar(cp)) {
				type = TokenType.PUNCTUATION;
			} else if (isNumberChar(cp)) {
				type = TokenType.NUMBER;
			} else if (Characters.isGraphic(cp)) {
				type = TokenType.EMOJI;
			} else {
				type = TokenType.GARBAGE;
			}

			if (type != previousType && current.length() > 0) {
				addToken(tokens, maxTokens, current.toString());
				current.setLength(0);
			}

			if (type == TokenType.GARBAGE) {
				if (current.length() == 0) current.append(MindReaderDictionary.GARBAGE);
			} else if (type == TokenType.EMOJI) {
				if (current.length() == 0) current.append(MindReaderDictionary.EMOJI);
			} else if (type == TokenType.NUMBER) {
				if (current.length() == 0) current.append(MindReaderDictionary.NUMBER);
			} else if (type != TokenType.SPACE) {
				current.appendCodePoint(cp);
			}

			previousType = type;
			i += step;
		}

		if (current.length() > 0) {
			addToken(tokens, maxTokens, current.toString());
		}

		if (textContainsEndingWord) {
			addToken(tokens, maxTokens, endingWord);
		}

		String[] validTokens = new String[Math.min(tokensCount, maxTokens)];
		System.arraycopy(tokens, Math.max(0, maxTokens - tokensCount), validTokens, 0, Math.min(tokensCount, maxTokens));
		return validTokens;
	}

	private static void addToken(String[] tokens, int maxTokens, String newToken) {
		tokensCount++;
		for (int i = 1; i < maxTokens; i++) {
			tokens[i - 1] = tokens[i];
		}
		tokens[maxTokens - 1] = newToken;
	}

	private static boolean isNumberChar(int cp) {
		return Character.isDigit(cp);
	}

	private static boolean isPunctuationChar(int cp) {
		for (int punctuationChar : MindReaderDictionary.PUNCTUATION) {
			if (cp == punctuationChar) {
				return true;
			}
		}
		return false;
	}

	private static boolean isWordChar(int cp, boolean allowApostrophe, boolean allowQuote) {
		return
			cp == 0x200C  // ZWNJ
			|| cp == 0x200D // ZWJ
			|| (allowApostrophe && cp == '\'')
			|| (allowQuote && cp == '"')
			|| Character.isLetter(cp)
			|| Character.getType(cp) == Character.NON_SPACING_MARK
			|| Character.getType(cp) == Character.COMBINING_SPACING_MARK;
	}
}
