package io.github.sspanak.tt9.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.util.chars.Characters;

public class TextTools {
	private static final Pattern CONTAINS_OTHER_THAN_1 = Pattern.compile("[02-9]");
	private static final Pattern COMBINING_STRING = Pattern.compile("^\\p{M}+$");
	private static final Pattern NEXT_IS_PUNCTUATION = Pattern.compile("^\\p{Punct}");
	private static final Pattern IS_CHINESE = Pattern.compile("\\p{script=Han}+");
	private static final Pattern IS_HANGUL = Pattern.compile("[\u1100-\u11FF\u302E-\u302F\u3131-\u318F\u3200-\u321F\u3260-\u327E\uA960-\uA97F\uAC00-\uD7FB\uFFA0-\uFFDF]+");
	private static final Pattern NEXT_TO_WORD = Pattern.compile("\\b$");
	private static final Pattern PREVIOUS_IS_LETTER = Pattern.compile("[\\p{L}\\p{M}]$");
	private static final Pattern START_OF_SENTENCE = Pattern.compile("(?<!\\.)(^|[.?!؟¿¡])\\s+$");


	public static boolean containsOtherThan1(String str) {
		return str != null && CONTAINS_OTHER_THAN_1.matcher(str).find();
	}


	public static boolean isCombining(String str) {
		return str != null && COMBINING_STRING.matcher(str).find();
	}


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


	public static boolean isChinese(String str) {
		return str != null && IS_CHINESE.matcher(str).find();
	}


	public static boolean isHangul(String str) {
		return str != null && IS_HANGUL.matcher(str).find();
	}


	public static int indexOfIgnoreCase(List<String> list, String str) {
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
		return str != null && START_OF_SENTENCE.matcher(str).find();
	}


	public static boolean isNextToWord(String str) {
		return str != null && NEXT_TO_WORD.matcher(str).find();
	}


	public static boolean nextIsPunctuation(String str) {
		return str != null && !str.isEmpty() && NEXT_IS_PUNCTUATION.matcher(str).find();
	}


	public static boolean previousIsLetter(String str) {
		return str != null && PREVIOUS_IS_LETTER.matcher(str).find();
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
}
