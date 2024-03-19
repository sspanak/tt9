package io.github.sspanak.tt9.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class TextTools {
	private static final Pattern containsOtherThan1 = Pattern.compile("[02-9]");
	private static final Pattern nextIsPunctuation = Pattern.compile("^\\p{Punct}");
	private static final Pattern nextToWord = Pattern.compile("\\b$");
	private static final Pattern previousIsLetter = Pattern.compile("\\p{L}$");
	private static final Pattern startOfSentence = Pattern.compile("(?<!\\.)(^|[.?!؟¿¡])\\s+$");


	public static boolean containsOtherThan1(String str) {
		return str != null && containsOtherThan1.matcher(str).find();
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


	public static boolean isStartOfSentence(String str) {
		return str != null && startOfSentence.matcher(str).find();
	}


	public static boolean isNextToWord(String str) {
		return str != null && nextToWord.matcher(str).find();
	}


	public static boolean nextIsPunctuation(String str) {
		return str != null && !str.isEmpty() && nextIsPunctuation.matcher(str).find();
	}


	public static boolean previousIsLetter(String str) {
		return str != null && previousIsLetter.matcher(str).find();
	}


	public static String unixTimestampToISODate(long timestamp) {
		if (timestamp < 0) {
			return "--";
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		sdf.setTimeZone(TimeZone.getDefault());

		return sdf.format(new Date(timestamp));
	}
}
