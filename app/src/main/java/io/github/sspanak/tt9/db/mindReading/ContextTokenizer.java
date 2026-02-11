package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.chars.Characters;

class ContextTokenizer {
	static final int WORD_SEPARATOR = 0x200b; // for languages where space has a punctuation-like role
	private enum TokenType {SPACE, WORD, PUNCTUATION, NUMBER, EMOJI, GARBAGE}

	@NonNull
	static String[] tokenize(@Nullable Language language, @NonNull String text, int maxTokens) {
		final StringBuilder current = new StringBuilder();
		final boolean isLangWithSpacePunctuation = LanguageKind.usesSpaceAsPunctuation(language);

		String[] tokens = new String[maxTokens];
		int tokensCount = 0;
		TokenType previousType = TokenType.SPACE;

		for (int i = 0, len = text.length(); i < len; ) {
			final int cp = text.codePointAt(i);
			final int step = Character.charCount(cp);
			final boolean isCpWhitespace = Character.isWhitespace(cp);

			TokenType type;
			if (cp == '\n' || cp == '\r') {
				current.setLength(0);
				tokens = new String[maxTokens];
				tokensCount = 0;
				type = TokenType.SPACE;
			} else if (isWordChar(cp) || isWordSpecialChar(language, cp)) {
				type = TokenType.WORD;
			} else if (isPunctuationChar(cp) || (isLangWithSpacePunctuation && isCpWhitespace)) {
				type = TokenType.PUNCTUATION;
			} else if (isNumberChar(cp)) {
				type = TokenType.NUMBER;
			} else if ((isCpWhitespace) || cp == WORD_SEPARATOR) {
				type = TokenType.SPACE;
			} else if (Characters.isGraphic(cp)) {
				type = TokenType.EMOJI;
			} else {
				type = TokenType.GARBAGE;
			}

			if (type != previousType && current.length() > 0) {
				addToken(tokens, maxTokens, current.toString());
				tokensCount++;
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
			tokensCount++;
		}

		String[] validTokens = new String[Math.min(tokensCount, maxTokens)];
		System.arraycopy(tokens, Math.max(0, maxTokens - tokensCount), validTokens, 0, Math.min(tokensCount, maxTokens));
		return validTokens;
	}

	private static void addToken(@NonNull String[] tokens, int maxTokens, @NonNull String newToken) {
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

	private static boolean isWordChar(int cp) {
		return
			Character.isLetter(cp)
			|| Character.getType(cp) == Character.NON_SPACING_MARK
			|| Character.getType(cp) == Character.COMBINING_SPACING_MARK;
	}

	private static boolean isWordSpecialChar(@Nullable Language language, int cp) {
		return switch (cp) {
			case '\'' -> LanguageKind.usesApostrophes(language);
			case '-' -> LanguageKind.isLatinBased(language) || LanguageKind.isCyrillic(language) || LanguageKind.isGreek(language);
			case '·' -> LanguageKind.isCatalan(language);
			case '"' -> LanguageKind.isHebrew(language);
			case Characters.ZWJ_CODE_POINT -> LanguageKind.isArabicBased(language) || LanguageKind.isIndic(language);
			case Characters.ZWNJ_CODE_POINT -> LanguageKind.isIndic(language);
			default -> false;
		};
	}
}
