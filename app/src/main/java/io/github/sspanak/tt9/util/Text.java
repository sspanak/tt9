
package io.github.sspanak.tt9.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.ime.helpers.InputConnectionAsync;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.chars.Characters;

public class Text extends TextTools {
	private static final String ALPHANUMERIC_CLASS = "1-9\\p{L}\\p{M}\\u200D\\u200C";
	private static final Pattern ALPHANUMERIC_AT_END = Pattern.compile("([" + ALPHANUMERIC_CLASS + "]+)$");
	private static final Pattern ALPHANUMERIC_WITH_APOSTROPHES_AT_END = Pattern.compile("([" + ALPHANUMERIC_CLASS + "']+)$");
	private static final Pattern ALPHANUMERIC_WITH_QUOTES_AT_END = Pattern.compile("([" + ALPHANUMERIC_CLASS + "\"]+)$");
	private static final Pattern ALPHANUMERIC_WITH_APOSTROPHES_AND_QUOTES_AT_END = Pattern.compile("([" + ALPHANUMERIC_CLASS + "\"']+)$");
	private static final Pattern ALPHANUMERIC_AT_START = Pattern.compile("^([" + ALPHANUMERIC_CLASS + "]+)");
	private static final Pattern ALPHANUMERIC_WITH_APOSTROPHES_AT_START = Pattern.compile("^([" + ALPHANUMERIC_CLASS + "']+)");
	private static final Pattern ALPHANUMERIC_WITH_QUOTES_AT_START = Pattern.compile("^([" + ALPHANUMERIC_CLASS + "\"]+)");
	private static final Pattern ALPHANUMERIC_WITH_APOSTROPHES_AND_QUOTES_AT_START = Pattern.compile("^([" + ALPHANUMERIC_CLASS + "\"']+)");

	private static final Pattern QUICK_DELETE_GROUP = Pattern.compile("(?:([\\s\\u3000]{2,})|([.,、。，،]{2,})|([^、。，\\s\\u3000]*.))$");

	private static final Pattern PREVIOUS_WORD = Pattern.compile("(?<=\\s|\\p{Punct}|^)([\\p{L}\\p{Mc}\\p{Mn}\\p{Me}\\x{200D}\\x{200C}]+)(?![\\r\\n])$");
	private static final Pattern PREVIOUS_WORD_WITH_FINAL_COMMA = Pattern.compile("(?<=\\s|\\p{Punct}|^)([\\p{L}\\p{Mc}\\p{Mn}\\p{Me}\\x{200D}\\x{200C}]+,?)(?![\\r\\n])$");
	private static final Pattern PREVIOUS_WORD_WITH_APOSTROPHES = Pattern.compile("(?<=\\s|^)([\\p{L}\\p{Mc}\\p{Mn}\\p{Me}\\x{200D}\\x{200C}']+)(?![\\r\\n])$");
	private static final Pattern PREVIOUS_WORD_WITH_APOSTROPHES_AND_FINAL_COMMA = Pattern.compile("(?<=\\s|^)([\\p{L}\\p{Mc}\\p{Mn}\\p{Me}\\x{200D}\\x{200C}']+,?)(?![\\r\\n])$");
	private static final Pattern PENULTIMATE_WORD = Pattern.compile("(?<=\\s|\\p{Punct}|^)([\\p{L}\\p{Mc}\\p{Mn}\\p{Me}\\x{200D}\\x{200C}]+)[\\s'][^\\s']*$");
	private static final Pattern PENULTIMATE_WORD_WITH_FINAL_COMMA = Pattern.compile("(?<=\\s|\\p{Punct}|^)([\\p{L}\\p{Mc}\\p{Mn}\\p{Me}\\x{200D}\\x{200C}]+,?)[\\s'][^\\s']*$");
	private static final Pattern PENULTIMATE_WORD_WITH_APOSTROPHES = Pattern.compile("(?<=\\s|^)([\\p{L}\\p{Mc}\\p{Mn}\\p{Me}\\x{200D}\\x{200C}']+)\\s\\S*$");
	private static final Pattern PENULTIMATE_WORD_WITH_APOSTROPHES_AND_FINAL_COMMA = Pattern.compile("(?<=\\s|^)([\\p{L}\\p{Mc}\\p{Mn}\\p{Me}\\x{200D}\\x{200C}']+,?)\\s\\S*$");

	@Nullable private final Language language;
	@Nullable private final String text;


	public Text(@Nullable Language language, @Nullable String text) {
		this.language = language;
		this.text = InputConnectionAsync.TIMEOUT_SENTINEL.equals(text) ? null : text;
	}


	public Text(@Nullable Language language, char text) {
		this(language, text == 0 ? null : String.valueOf(text));
	}


	public Text(@Nullable String text) {
		this(null, text);
	}


	public String capitalize() {
		if (language == null || text == null || text.isEmpty() || !language.hasUpperCase() || !Character.isAlphabetic(text.charAt(0))) {
			return text;
		}

		char[] chars = text.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return String.valueOf(chars);
	}


	public boolean isValidWordWithPunctuation(List<Character> punctuation) {
		for (int i = 0, end = text != null ? text.length() : 0; i < end; i++) {
			if (!Character.isAlphabetic(text.charAt(i)) && !punctuation.contains(text.charAt(i))) {
				return false;
			}
		}

		return true;
	}


	public boolean endsWithLetter() {
		if (text == null || text.isEmpty() || Character.isWhitespace(text.charAt(text.length() - 1))) {
			return false;
		}

		BreakIterator bi = BreakIterator.getCharacterInstance(language != null ? language.getLocale() : Locale.getDefault());
		bi.setText(text);

		for (int end = bi.last(), i = bi.preceding(end); i < end; i++) {
			if (!Character.isAlphabetic(text.charAt(i))) {
				return false;
			}
		}

		return true;
	}


	@NonNull
	public String getPreviousWord(boolean skipOne, boolean includeApostrophes, boolean allowFinalComma) {
		if (text == null || text.isEmpty()) {
			return "";
		}

		Pattern pattern;
		if (allowFinalComma && includeApostrophes) {
			pattern = skipOne ? PENULTIMATE_WORD_WITH_APOSTROPHES_AND_FINAL_COMMA : PREVIOUS_WORD_WITH_APOSTROPHES_AND_FINAL_COMMA;
		} else if (allowFinalComma) {
			pattern = skipOne ? PENULTIMATE_WORD_WITH_FINAL_COMMA : PREVIOUS_WORD_WITH_FINAL_COMMA;
		} else if (includeApostrophes) {
			// In Ukrainian and Hebrew, apostrophes are part of the word, so count them as "letters"
			pattern = skipOne ? PENULTIMATE_WORD_WITH_APOSTROPHES : PREVIOUS_WORD_WITH_APOSTROPHES;
		} else {
			pattern = skipOne ? PENULTIMATE_WORD : PREVIOUS_WORD;
		}

		final Matcher matcher = pattern.matcher(text);
		final String word = matcher.find() ? matcher.group(1) : null;
		return word == null ? "" : word;
	}


	public int getTextCase() {
		if (isUpperCase()) {
			return InputMode.CASE_UPPER;
		} else if (isCapitalized()) {
			return InputMode.CASE_CAPITALIZE;
		} else if (isMixedCase()) {
			return InputMode.CASE_DICTIONARY;
		} else {
			return InputMode.CASE_LOWER;
		}
	}


	public boolean isAlphabetic() {
		for (int i = 0, end = text == null ? 0 : text.length(); i < end; i++) {
			if (!Character.isAlphabetic(text.charAt(i))) {
				return false;
			}
		}

		return true;
	}


	public boolean isNumeric() {
		if (text == null) {
			return false;
		}

		for (int i = 0, end = text.length(); i < end; i++) {
			if (!Character.isDigit(text.charAt(i))) {
				return false;
			}
		}

		return true;
	}


	public boolean isWord() {
		boolean isApostropheAllowed = LanguageKind.isUkrainian(language) || LanguageKind.isHebrew(language);
		for (int i = 0, end = text == null ? 0 : text.length(); i < end; i++) {
			if (!Character.isAlphabetic(text.charAt(i)) && !(isApostropheAllowed && text.charAt(i) == '\'')) {
				return false;
			}
		}

		return true;
	}


	public boolean isEmpty() {
		return text == null || text.isEmpty();
	}


	private boolean isCapitalized() {
		if (text == null || text.length() < 2) {
			return false;
		}

		char[] chars = text.toCharArray();
		boolean firstLetterFound = false;

		for (int i = 0, end = text.length(); i < end; i++) {
			if (!Character.isAlphabetic(chars[i])) {
				continue;
			}

			if (!firstLetterFound) {
				if (Character.isUpperCase(chars[i])) {
					firstLetterFound = true;
					continue;
				} else {
					return false;
				}
			}

			if (Character.isUpperCase(chars[i])) {
				return false;
			}
		}

		return true;
	}


	public boolean isMixedCase() {
		return
			language != null
			&& text != null
			&& !text.toLowerCase(language.getLocale()).equals(text)
			&& !text.toUpperCase(language.getLocale()).equals(text);
	}


	public boolean isUpperCase() {
		return language != null && text != null && language.hasUpperCase() && text.toUpperCase(language.getLocale()).equals(text);
	}


	/**
	 * Returns the number of regular 8-bit chars
	 */
	public int length() {
		return text == null ? 0 : text.length();
	}


	/**
	 * Returns the number of UTF-16 chars
	 */
	public int codePointLength() {
		return text == null ? 0 : text.codePointCount(0, text.length());
	}


	@NonNull
	public String deleteCharAt(int index) {
		if (text == null) {
			return "";
		}

		if (index < 0 || index >= text.length()) {
			return text;
		}

		StringBuilder sb = new StringBuilder(text);
		sb.deleteCharAt(index);
		return sb.toString();
	}


	@NonNull
	public String duplicateCharAt(int position) {
		if (text == null) {
			return "";
		}

		if (position < 0 || position >= text.length()) {
			return text;
		}

		final int length = text.length();
		final char[] oldText = text.toCharArray();
		final char[] newText = new char[length + 1];

		System.arraycopy(oldText, 0, newText, 0, position);
		newText[position] = oldText[position];
		System.arraycopy(oldText, position, newText, position + 1, length - position);
		return new String(newText);
	}


	/**
	 * Returns the length of the last grapheme (a user-perceived character). This allows for correctly
	 * deleting graphemes consisting of multiple Java chars. Example: 🏴‍☠️ (pirate flag) = 5 chars.
	 */
	public int lastGraphemeLength() {
		if (text == null) {
			return 0;
		}

		if (text.length() <= 1) {
			return text.length();
		}

		BreakIterator bi = BreakIterator.getCharacterInstance(language != null ? language.getLocale() : Locale.getDefault());
		bi.setText(text);
		final int end = bi.last();
		return end - bi.preceding(end);
}


	/**
	 * Returns the starting index of the last word boundary. This could be start of a word, of a whitespace
	 * block or of a punctuation block (e.g. "..."). In the case of languages that do not use spaces
	 * it is not possible to determine where a word starts, so in case the text ends with letters only,
	 * we assume the last word is at most MAX_WORD_LENGTH_NO_SPACE letters long.
	 */
	public int lastBoundaryIndex(final int MAX_WORD_LENGTH_NO_SPACE) {
		if (text == null || text.length() < 2) {
			return -1;
		}

		Matcher matcher = QUICK_DELETE_GROUP.matcher(text);
		for (int i = matcher.find() ? matcher.groupCount() : 0, nonLetterGroup = 0; i >= 0; i--, nonLetterGroup = 1) {
			String group = matcher.group(i);
			if (group == null) {
				continue;
			}

			if (nonLetterGroup == 1) {
				return matcher.start();
			}

			Text gr = new Text(group);
			int codePoints = gr.codePointLength();

			// In the writing systems that do not use spaces, the last group is not the last word, but
			// it could be an entire sentence or a paragraph. That's why we assume an average word length
			// of N letters
			if (codePoints > MAX_WORD_LENGTH_NO_SPACE && (TextTools.isChineseText(group) || TextTools.isJapaneseText(group) || TextTools.isThaiText(group))) {
				return text.length() - gr.substringCodePoints(codePoints - 4, codePoints).length();
			} else {
				return matcher.start();
			}
		}

		return 0;
	}


	public boolean startsWithWhitespace() {
		return text != null && !text.isEmpty() && Character.isWhitespace(text.charAt(0)) && !text.startsWith("\n");
	}


	public boolean startsWithNewline() {
		return text != null && !text.isEmpty() && text.charAt(0) == '\n';
	}


	public boolean startsWithNumber() {
		return text != null && !text.isEmpty() && Character.isDigit(text.charAt(0));
	}


	public boolean startsWithGraphic() {
		return text != null && !text.isEmpty() && Characters.isGraphic(text.charAt(0));
	}


	public boolean startsWithWord() {
		return text != null && !text.isEmpty() && Character.isAlphabetic(text.charAt(0));
	}


	/**
	 * A safe substring method that works with code points (UTF-16 chars), instead of 8-bit chars.
	 * Useful for languages with complex characters, like Chinese.
	 */
	public String substringCodePoints(int start, int end) {
		if (text == null) {
			return "";
		}

		if (!LanguageKind.isCJK(language)) {
			return text.substring(start, end);
		}

		StringBuilder output = new StringBuilder();
		for (int i = Math.max(start, 0), finish = Math.min(text.length(), end); i < finish; i++) {
			output.append(text.charAt(i));
		}

		return output.toString();
	}


	public String subStringEndingAlphanumeric(boolean keepApostrophe, boolean keepQuote) {
		if (text == null || text.isEmpty()) {
			return "";
		}

		Pattern pattern;
		if (keepApostrophe && keepQuote) {
			pattern = ALPHANUMERIC_WITH_APOSTROPHES_AND_QUOTES_AT_END;
		} else if (keepQuote) {
			pattern = ALPHANUMERIC_WITH_QUOTES_AT_END;
		} else if (keepApostrophe) {
			pattern = ALPHANUMERIC_WITH_APOSTROPHES_AT_END;
		} else {
			pattern = ALPHANUMERIC_AT_END;
		}

		Matcher matcher = pattern.matcher(text);

		return matcher.find() ? matcher.group(1) : "";
	}


	public String subStringStartingAlphanumeric(boolean keepApostrophe, boolean keepQuote) {
		if (text == null || text.isEmpty()) {
			return "";
		}

		Pattern pattern;

		if (keepApostrophe && keepQuote) {
			pattern = ALPHANUMERIC_WITH_APOSTROPHES_AND_QUOTES_AT_START;
		} else if (keepQuote) {
			pattern = ALPHANUMERIC_WITH_QUOTES_AT_START;
		} else if (keepApostrophe) {
			pattern = ALPHANUMERIC_WITH_APOSTROPHES_AT_START;
		} else {
			pattern = ALPHANUMERIC_AT_START;
		}

		Matcher matcher = pattern.matcher(text);

		return matcher.find() ? matcher.group(1) : "";
	}


	public String toLowerCase() {
		if (text == null) {
			return "";
		} else {
			return text.toLowerCase(language != null ? language.getLocale() : Locale.getDefault());
		}
	}


	public String toUpperCase() {
		if (text == null) {
			return "";
		} else {
			return text.toUpperCase(language != null ? language.getLocale() : Locale.getDefault());
		}
	}


	public String toTextCase(int textCase) {
		return switch (textCase) {
			case InputMode.CASE_LOWER -> toLowerCase();
			case InputMode.CASE_UPPER -> toUpperCase();
			case InputMode.CASE_CAPITALIZE -> capitalize();
			default -> text == null ? "" : text;
		};
	}


	@NonNull
	@Override
	public String toString() {
		return text == null ? "" : text;
	}
}
