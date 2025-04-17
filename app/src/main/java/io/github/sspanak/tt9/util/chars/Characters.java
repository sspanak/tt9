package io.github.sspanak.tt9.util.chars;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;

public class Characters extends Emoji {
	public static final String COMBINING_ZERO_BASE = "◌";
	public static final String IDEOGRAPHIC_SPACE = "　";

	final public static ArrayList<String> Currency = new ArrayList<>(Arrays.asList(
		"$", "€", "₿", "¢", "¤", "₱", "¥", "£"
	));

	/**
	 * The English punctuation filtered to contain only valid email characters.
	 */
	final public static ArrayList<ArrayList<String>> Email = new ArrayList<>(Arrays.asList(
		new ArrayList<>(Arrays.asList("@", "_", " ", "#", "%", "{", "}", "|", "^", "/", "=", "*", "+")),
		new ArrayList<>(Arrays.asList(".", "-", "&", "~", "`", "'", "!", "?"))
	));

	/**
	 * Special characters for phone number fields, including both characters for conveniently typing a phone number: "()-",
	 * as well as command characters such as "," = "slight pause" and ";" = "wait" used in Japan and some other countries.
	 */
	final public static ArrayList<ArrayList<String>> Phone = new ArrayList<>(Arrays.asList(
		new ArrayList<>(Arrays.asList("+", " ")),
		new ArrayList<>(Arrays.asList("-", "(", ")", ".", ";", ","))
	));

	/**
	 * Returns the language-specific space character.
	 */
	public static String getSpace(Language language) {
		return LanguageKind.isChinese(language) || LanguageKind.isJapanese(language) ? IDEOGRAPHIC_SPACE : " ";
	}

	/**
	 * Standard special characters with automatic Space selection based on the language. Useful for
	 * text fields.
	 */
	public static ArrayList<String> getSpecial(Language language) {
		return new ArrayList<>(Arrays.asList(
			getSpace(language), "\n", "@", "_", "#", "%", "[", "]", "{", "}", "§", "|", "^", "<", ">", "\\", "/", "=", "*", "+"
		));
	}

	/**
	 * Special characters for all kinds of numeric fields: integer, decimal with +/- included as necessary.
	 */
	public static ArrayList<ArrayList<String>> getSpecialForNumbers(boolean decimal, boolean signed) {
		ArrayList<ArrayList<String>> keyCharacters = new ArrayList<>();
		keyCharacters.add(signed ? new ArrayList<>(Arrays.asList("-", "+")) : new ArrayList<>());
		if (decimal) {
			keyCharacters.add(new ArrayList<>(Arrays.asList(".", ",")));
		}
		return keyCharacters;
	}

	public static boolean isCurrency(Language language, String c) {
		return Currency.contains(c) || (language != null && language.getCurrency().equals(c));
	}

	public static boolean isFathatan(char ch) {
		return ch == 0x064B;
	}
}
