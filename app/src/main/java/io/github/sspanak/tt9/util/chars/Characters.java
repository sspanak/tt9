package io.github.sspanak.tt9.util.chars;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;

public class Characters extends Emoji {
	public static final String PLACEHOLDER = "◌";
	public static final String IDEOGRAPHIC_SPACE = "　";
	public static final String TAB = "↹";


	public static final ArrayList<String> Currency = new ArrayList<>(Arrays.asList(
		"$", "€", "₿", "¢", "¤", "₱", "¥", "£"
	));


	/**
	 * The English punctuation filtered to contain only valid email characters.
	 */
	public static final ArrayList<ArrayList<String>> Email = new ArrayList<>(Arrays.asList(
		new ArrayList<>(Arrays.asList("@", "_", " ", "#", "%", "{", "}", "|", "^", "/", "=", "*", "+")),
		new ArrayList<>(Arrays.asList(".", "-", "&", "~", "`", "'", "!", "?"))
	));


	/**
	 * Special characters for phone number fields, including both characters for conveniently typing a phone number: "()-",
	 * as well as command characters such as "," = "slight pause" and ";" = "wait" used in Japan and some other countries.
	 */
	public static final ArrayList<ArrayList<String>> Phone = new ArrayList<>(Arrays.asList(
		new ArrayList<>(Arrays.asList("+", " ")),
		new ArrayList<>(Arrays.asList("-", "(", ")", ".", ";", ","))
	));



	/**
	 * Commonly used special and math characters.
	 */
	public static final ArrayList<String> Special = new ArrayList<>(Arrays.asList(
		"@", "_", "#", "%", "[", "]", "{", "}", "§", "|", "^", "<", ">", "\\", "/", "=", "*", "+"
	));


	/**
	 * Returns a language-specific currency list.
	 */
	public static ArrayList<String> getCurrencies(@Nullable Language language) {
		ArrayList<String> chars = new ArrayList<>(Characters.Currency);
		if (language != null && !language.getCurrency().isEmpty()) {
			chars.add(2, language.getCurrency());
		}
		return chars;
	}


	/**
	 * Returns the language-specific space character.
	 */
	public static String getSpace(@Nullable Language language) {
		return LanguageKind.isChinese(language) || LanguageKind.isJapanese(language) ? IDEOGRAPHIC_SPACE : " ";
	}


	/**
	 * Whitespace characters with language-specific Space. Useful for text fields.
	 */
	public static ArrayList<String> getWhitespaces(@Nullable Language language) {
		return new ArrayList<>(Arrays.asList(
			getSpace(language), "\n", "\t"
		));
	}


	/**
	 * Special and punctuation characters for all kinds of numeric fields: integer, decimal with +/-,
	 * included as necessary.
	 */
	public static ArrayList<ArrayList<String>> getAllForDecimal(boolean decimal, boolean signed) {
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


	public static boolean isOm(char ch) {
		return
			ch == 0x0950 // Devanagari
			|| ch == 0x0AD0; // Gujarati
	}
}
