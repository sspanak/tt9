package io.github.sspanak.tt9;

import java.util.regex.Pattern;

public class TextTools {
	private static final Pattern containsOtherThan1 = Pattern.compile("[02-9]");
	private static final Pattern previousIsLetter = Pattern.compile("\\p{L}$");
	private static final Pattern nextIsPunctuation = Pattern.compile("^\\p{Punct}");
	private static final Pattern nextToWord = Pattern.compile("\\b$");
	private static final Pattern startOfSentence = Pattern.compile("(?<!\\.)(^|[.?!¿¡])\\s*$");

	public static boolean containsOtherThan1(String str) {
		return str != null && containsOtherThan1.matcher(str).find();
	}

	public static boolean isNextToWord(String str) {
		return str != null && nextToWord.matcher(str).find();
	}

	public static boolean isStartOfSentence(String str) {
		return str != null && startOfSentence.matcher(str).find();
	}

	public static boolean nextIsPunctuation(String str) {
		return str != null && nextIsPunctuation.matcher(str).find();
	}

	public static boolean previousIsLetter(String str) {
		return str != null && previousIsLetter.matcher(str).find();
	}

	public static boolean startsWithWhitespace(String str) {
		return str != null && !str.isEmpty() && (str.charAt(0) == ' ' || str.charAt(0) == '\n' || str.charAt(0) == '\t');
	}

	public static boolean startsWithNumber(String str) {
		return str != null && !str.isEmpty() && (str.charAt(0) >= '0' && str.charAt(0) <= '9');
	}

	public static String removeNonLetters(String str) {
		return str != null ? str.replaceAll("\\P{L}", "") : null;
	}
}
