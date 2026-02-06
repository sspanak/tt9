package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.util.chars.Characters;

class ContextTokenizer {
	private enum TokenType {SPACE, WORD, PUNCTUATION, NUMBER, EMOJI, GARBAGE}

	private static int tokensCount;

	@NonNull
	static String[] tokenize(@NonNull String text, int maxTokens, boolean allowApostrophe, boolean allowQuote) {
		final StringBuilder current = new StringBuilder();
		final String[] tokens = new String[maxTokens];

		tokensCount = 0;
		TokenType previousType = TokenType.SPACE;

		for (int i = 0, len = text.length(); i < len; ) {
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

		String[] validTokens = new String[Math.min(tokensCount, maxTokens)];
		System.arraycopy(tokens, Math.max(0, maxTokens - tokensCount), validTokens, 0, Math.min(tokensCount, maxTokens));
		return validTokens;
	}

	private static void addToken(@NonNull String[] tokens, int maxTokens, @NonNull String newToken) {
		tokensCount++;
		for (int i = 1; i < maxTokens; i++) {
			tokens[i - 1] = tokens[i];
		}
		tokens[maxTokens - 1] = newToken;
	}

	private static boolean isNumberChar(int cp) {
		return Character.isDigit(cp);
	}

	static boolean isPunctuationChar(int cp) {
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
