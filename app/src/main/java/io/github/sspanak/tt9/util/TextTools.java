package io.github.sspanak.tt9.util;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.ime.helpers.InputConnectionAsync;
import io.github.sspanak.tt9.util.chars.Characters;

public class TextTools {
	private static final Pattern COMBINING_STRING = Pattern.compile("^\\p{M}+$");
	private static final Pattern CONTAINS_PUNCTUATION = Pattern.compile("\\p{Punct}");
	private static final Pattern NEXT_IS_PUNCTUATION = Pattern.compile("^\\p{Punct}");
	private static final Pattern IS_CHINESE = Pattern.compile("[\\p{script=Han}" + String.join("", Characters.PunctuationChinese) + "]+");
	private static final Pattern IS_JAPANESE = Pattern.compile("[\\p{script=Hiragana}\\p{script=Katakana}\\p{script=Han}" + String.join("", Characters.PunctuationChinese) + "]+");
	private static final Pattern IS_HANGUL_TEXT = Pattern.compile("[\u1100-\u11FF\u302E-\u302F\u3131-\u318F\u3200-\u321F\u3260-\u327E\uA960-\uA97F\uAC00-\uD7FB\uFFA0-\uFFDF]+");
	private static final Pattern IS_CHINESE_TEXT = Pattern.compile("\\p{script=Han}+");
	private static final Pattern IS_JAPANESE_TEXT = Pattern.compile("[\\p{script=Hiragana}\\p{script=Katakana}\\p{script=Han}]+");
	private static final Pattern IS_THAI_TEXT = Pattern.compile("[\\u0E00-\\u0E7F]+");
	private static final Pattern NEXT_TO_WORD = Pattern.compile("\\b$");
	private static final Pattern START_OF_SENTENCE = Pattern.compile("(?<!\\.)(^|[.?!؟¿¡])\\s+$");


	public static boolean isSingleCodePoint(String str) {
		return
			str != null
			&& !str.isEmpty()
			&& str.offsetByCodePoints(0, 1) == str.length();
	}


	public static boolean containsPunctuation(String str) {
		return str != null && !str.isEmpty() && CONTAINS_PUNCTUATION.matcher(str).find();
	}


	public static boolean isCombining(String str) {
		return str != null && COMBINING_STRING.matcher(str).find();
	}


	/**
	 * Validates if the string contains only graphic characters (e.g. emojis)
	 */
	public static boolean isGraphic(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}

		for (int i = 0, end = str.length(); i < end; i++) {
			if (!Characters.isGraphic(str.charAt(i))) {
				return false;
			}
		}

		return true;
	}


	/**
	 * Validates Chinese text and punctuation.
	 */
	public static boolean isChinese(String str) {
		return str != null && IS_CHINESE.matcher(str).find();
	}


	/**
	 * Validates Japanese text and punctuation.
	 */
	public static boolean isJapanese(String str) {
		return str != null && IS_JAPANESE.matcher(str).find();
	}


	/**
	 * Validates Chinese text only, but no punctuation.
	 */
	public static boolean isChineseText(String str) {
		return str != null && IS_CHINESE_TEXT.matcher(str).find();
	}


	/**
	 * Validates Japanese text only, but no punctuation.
	 */
	public static boolean isJapaneseText(String str) {
		return str != null && IS_JAPANESE_TEXT.matcher(str).find();
	}


	/**
	 * Validates Korean text only, but no punctuation.
	 */
	public static boolean isHangulText(String str) {
		return str != null && IS_HANGUL_TEXT.matcher(str).find();
	}


	/**
	 * Validates Thai text only, but no punctuation.
	 */
	public static boolean isThaiText(String str) {
		return str != null && IS_THAI_TEXT.matcher(str).find();
	}


	public static int indexOfIgnoreCase(@Nullable List<String> list, @Nullable String str) {
		for (int i = 0, size = list != null && str != null ? list.size() : 0; i < size; i++) {
			if (list.get(i).equalsIgnoreCase(str)) {
				return i;
			}
		}

		return -1;
	}


	public static int lastIndexOfLatin(String str) {
		for (int i = str != null ? str.length() - 1 : -1; i >= 0; i--) {
			char ch = str.charAt(i);
			if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
				return i;
			}
		}

		return -1;
	}


	public static boolean isStartOfSentence(String str) {
		return str != null && !str.equals(InputConnectionAsync.TIMEOUT_SENTINEL) && START_OF_SENTENCE.matcher(str).find();
	}


	public static boolean isNextToWord(String str) {
		return str != null && !str.equals(InputConnectionAsync.TIMEOUT_SENTINEL) && NEXT_TO_WORD.matcher(str).find();
	}


	public static boolean nextIsPunctuation(String str) {
		return str != null && !str.isEmpty() && NEXT_IS_PUNCTUATION.matcher(str).find();
	}


	public static String unixTimestampToISODate(long timestamp) {
		if (timestamp < 0) {
			return "--";
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		sdf.setTimeZone(TimeZone.getDefault());

		return sdf.format(new Date(timestamp));
	}


	public static ArrayList<String> removeLettersFromList(ArrayList<String> list) {
		ArrayList<String> cleanList = new ArrayList<>();
		for (String ch : list) {
			if (!Character.isAlphabetic(ch.codePointAt(0))) {
				cleanList.add(ch);
			}
		}

		return cleanList;
	}


	public static String removeNonLettersFromListAndJoin(ArrayList<String> list) {
		StringBuilder cleanList = new StringBuilder();
		for (String ch : list) {
			if (Character.isAlphabetic(ch.codePointAt(0))) {
				cleanList.append(ch);
			}
		}

		return cleanList.toString();
	}
}
