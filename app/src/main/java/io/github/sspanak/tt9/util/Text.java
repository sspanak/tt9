package io.github.sspanak.tt9.util;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Locale;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.chars.Characters;

public class Text extends TextTools {
	private final Language language;
	private final String text;


	public Text(Language language, String text) {
		this.language = language;
		this.text = text;
	}


	public Text(String text) {
		this.language = null;
		this.text = text;
	}


	public String capitalize() {
		if (language == null || text == null || text.isEmpty() || !language.hasUpperCase()) {
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


	public boolean endsWithGraphic() {
		return text != null && !text.isEmpty() && Characters.isGraphic(text.charAt(text.length() - 1));
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


	public boolean isEmpty() {
		return text == null || text.isEmpty();
	}


	private boolean isCapitalized() {
		if (text == null || text.length() < 2) {
			return false;
		}

		if (!Character.isUpperCase(text.charAt(0))) {
			return false;
		}

		for (int i = 1, end = text.length(); i < end; i++) {
			if (Character.isUpperCase(text.charAt(i))) {
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
		return language != null && text != null && text.toUpperCase(language.getLocale()).equals(text);
	}


	public String leaveEndingGraphics() {
		if (text == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder(text.length());

		for (int i = text.length() - 1; i >= 0; i--) {
			char ch = text.charAt(i);

			if (Characters.isGraphic(ch)) {
				sb.insert(0, ch);
			} else {
				break;
			}
		}

		return sb.toString();
	}


	public String leaveStartingGraphics() {
		if (text == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder(text.length());

		for (int i = 0, end = text.length(); i < end; i++) {
			char ch = text.charAt(i);

			if (Characters.isGraphic(ch)) {
				sb.append(ch);
			} else {
				break;
			}
		}

		return sb.toString();
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


	public int lastBoundaryIndex() {
		if (text == null || text.length() < 2) {
			return -1;
		}

		char lastChar = text.charAt(text.length() - 1);
		char penultimateChar = text.charAt(text.length() - 2);

		boolean endsWithWhitespaceBlock = Character.isWhitespace(lastChar) && Character.isWhitespace(penultimateChar);
		boolean endsWithPunctuationBlock = (lastChar == '.' || lastChar == ',') && (penultimateChar == '.' || penultimateChar == ',');

		for (int i = text.length() - 1, firstChar = 1; i >= 0; i--, firstChar = 0) {
			char currentChar = text.charAt(i);

			if (
				(endsWithPunctuationBlock && currentChar != '.' && currentChar != ',')
				|| (endsWithWhitespaceBlock && !Character.isWhitespace(currentChar))
				|| (!endsWithWhitespaceBlock && !endsWithPunctuationBlock && firstChar == 0 && Character.isWhitespace(currentChar))
			) {
				return i + 1;
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


	public String subStringEndingWord(boolean keepApostrophe, boolean keepQuote) {
		if (text == null) {
			return "";
		}

		StringBuilder sub = new StringBuilder();

		for (int i = text.length() - 1; i >= 0; i--) {
			char ch = text.charAt(i);

			if (Character.isAlphabetic(ch) || (keepApostrophe && ch == '\'') || (keepQuote && ch == '"')) {
				sub.insert(0, ch);
			} else {
				break;
			}
		}

		return sub.toString();
	}


	public String subStringStartingWord(boolean keepApostrophe, boolean keepQuote) {
		if (text == null) {
			return "";
		}

		StringBuilder sub = new StringBuilder();

		for (int i = 0, end = text.length(); i < end; i++) {
			char ch = text.charAt(i);

			if (Character.isAlphabetic(ch) || (keepApostrophe && ch == '\'') || (keepQuote && ch == '"')) {
				sub.append(ch);
			} else {
				break;
			}
		}

		return sub.toString();
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


	@NonNull
	@Override
	public String toString() {
		return text == null ? "" : text;
	}
}
