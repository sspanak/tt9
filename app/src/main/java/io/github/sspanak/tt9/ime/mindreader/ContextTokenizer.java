package io.github.sspanak.tt9.ime.mindreader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.chars.Characters;

class ContextTokenizer {
	// alternative space for languages where space has a punctuation-like role
	static final int WORD_SEPARATOR = 0x200b;
	private static final String WORD_SEPARATOR_STR = "\u200b";

	private enum TokenType {SPACE, WORD, PUNCTUATION_PRIORITY, PUNCTUATION_OTHER, NUMBER, EMOJI, GARBAGE}


	@NonNull
	static String[] tokenize(@NonNull MindReaderDictionary dictionary, @Nullable Language language, @NonNull String text, @Nullable String unusedBeforeCursor, int maxTokens) {
		if (text.isEmpty()) {
			return new String[0];
		}

		// split by space, if possible
		String[] tokens = tokenizeTextWithSpace(language, text, maxTokens);
		if (tokens.length >= maxTokens || language == null || language.hasSpaceBetweenWords() || unusedBeforeCursor == null || unusedBeforeCursor.isEmpty()) {
			return tokens;
		}

		// in languages without space, if there aren't enough tokens, try to extract more from the
		// unused text before the cursor. Since it would be a solid block of text, we use the dictionary
		// to search for words.

		// remove the tokens that we already extracted above
		final String squashedText = text.replace(WORD_SEPARATOR_STR, "");
		if (unusedBeforeCursor.endsWith(squashedText)) {
			unusedBeforeCursor = unusedBeforeCursor.substring(0, unusedBeforeCursor.length() - squashedText.length());
		}

		// remove the dummy start-of-text character
		final String dot = Characters.getChar(language, ".");
		if (dot != null && unusedBeforeCursor.startsWith(dot)) {
			unusedBeforeCursor = unusedBeforeCursor.substring(dot.length());
		}

		// tokenize the remaining text using the dictionary
		final String[] beforeTokens = tokenizeWithoutSpace(language, dictionary, unusedBeforeCursor, maxTokens - tokens.length);
		if (beforeTokens.length == 0) {
			return tokens;
		}

		String[] allTokens = new String[Math.min(tokens.length + beforeTokens.length, maxTokens)];
		System.arraycopy(beforeTokens, 0, allTokens, 0, beforeTokens.length);
		System.arraycopy(tokens, 0, allTokens, beforeTokens.length, tokens.length);

		return allTokens;
	}


	private static String[] tokenizeTextWithSpace(@Nullable Language language, @NonNull String text, int maxTokens) {
		if (text.isEmpty() || maxTokens <= 0) {
			return new String[0];
		}

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
			} else if (isWordChar(cp) || (previousType == TokenType.WORD && isWordSpecialChar(language, cp))) {
				// independent special word chars are not considered words themselves,
				// otherwise we get undesired false-positives, such as "-" being considered a word
				type = TokenType.WORD;
			} else if (isPriorityPunctuationChar(cp)) {
				type = TokenType.PUNCTUATION_PRIORITY;
			} else if (isOtherPunctuationChar(cp) || (isLangWithSpacePunctuation && isCpWhitespace)) {
				type = TokenType.PUNCTUATION_OTHER;
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
			} else if (type == TokenType.PUNCTUATION_OTHER) {
				if (current.length() == 0) current.append(MindReaderDictionary.PUNCTUATION_OTHER);
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


	private static String[] tokenizeWithoutSpace(@NonNull Language language, @NonNull MindReaderDictionary dictionary, @NonNull String text, int maxTokens) {
		if (text.isEmpty() || maxTokens <= 0) {
			return new String[0];
		}

		final boolean isLangWithSpacePunctuation = LanguageKind.usesSpaceAsPunctuation(language);

		String[] tokens = new String[maxTokens];
		int tokensCount = 0;
		int i = text.length();

		while (i > 0 && tokensCount < maxTokens) {
			final int cp = text.codePointBefore(i);

			if (cp == '\n' || cp == '\r') {
				break;
			}

			final boolean isCpWhitespace = Character.isWhitespace(cp);

			if (isPriorityPunctuationChar(cp)) {
				addToken(tokens, maxTokens, new String(Character.toChars(cp)));
				tokensCount++;
				i -= Character.charCount(cp);
				continue;
			}

			if (isOtherPunctuationChar(cp) || (isLangWithSpacePunctuation && isCpWhitespace)) {
				addToken(tokens, maxTokens, MindReaderDictionary.PUNCTUATION_OTHER);
				tokensCount++;
				i -= Character.charCount(cp);
				continue;
			}

			if (Character.isDigit(cp)) {
				addToken(tokens, maxTokens, MindReaderDictionary.NUMBER);
				tokensCount++;
				i -= Character.charCount(cp);
				continue;
			}

			if (isCpWhitespace) {
				addToken(tokens, maxTokens, MindReaderDictionary.SPACE);
				tokensCount++;
				i -= Character.charCount(cp);
				continue;
			}

			if (Characters.isGraphic(cp)) {
				addToken(tokens, maxTokens, MindReaderDictionary.EMOJI);
				tokensCount++;
				i -= Character.charCount(cp);
				continue;
			}

			final String match = dictionary.getLongestWord(text, i);

			if (match != null) {
				addToken(tokens, maxTokens, match);
				tokensCount++;
				i -= match.length();
			} else {
				// fallback: single char as garbage
				addToken(tokens, maxTokens, MindReaderDictionary.GARBAGE);
				tokensCount++;
				i -= Character.charCount(cp);
			}
		}

		// trim and reverse the result
		final String[] result = new String[Math.min(tokensCount, maxTokens)];
		for (int j = 0; j < result.length; j++) {
			result[j] = tokens[maxTokens - 1 - j];
		}

		return result;
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


	static boolean isPriorityPunctuationChar(int cp) {
		for (int punctuationChar : MindReaderDictionary.PUNCTUATION) {
			if (cp == punctuationChar) {
				return true;
			}
		}

		return false;
	}


	public static boolean isOtherPunctuationChar(int codePoint) {
		int type = Character.getType(codePoint);
		return
			type == Character.CONNECTOR_PUNCTUATION
			|| type == Character.DASH_PUNCTUATION
			|| type == Character.START_PUNCTUATION
			|| type == Character.END_PUNCTUATION
			|| type == Character.INITIAL_QUOTE_PUNCTUATION
			|| type == Character.FINAL_QUOTE_PUNCTUATION
			|| type == Character.OTHER_PUNCTUATION;
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
