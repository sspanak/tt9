package io.github.sspanak.tt9.languages;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class Text {
	private static final Pattern containsOtherThan1 = Pattern.compile("[02-9]");
	private static final Pattern previousIsLetter = Pattern.compile("\\p{L}$");
	private static final Pattern nextIsPunctuation = Pattern.compile("^\\p{Punct}");
	private static final Pattern nextToWord = Pattern.compile("\\b$");
	private static final Pattern startOfSentence = Pattern.compile("(?<!\\.)(^|[.?!؟¿¡])\\s+$");


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

		if (text.length() == 1) {
			return text.toUpperCase(language.getLocale());
		} else {
			return text.substring(0, 1).toUpperCase(language.getLocale()) + text.substring(1);
		}
	}

	public static boolean containsOtherThan1(String str) {
		return str != null && containsOtherThan1.matcher(str).find();
	}

	public boolean isEmpty() {
		return text == null || text.isEmpty();
	}

	public boolean isMixedCase() {
		return
			language != null
			&& text != null
			&& !text.toLowerCase(language.getLocale()).equals(text)
			&& !text.toUpperCase(language.getLocale()).equals(text);
	}

	public boolean isNextToWord() {
		return text != null && nextToWord.matcher(text).find();
	}

	public boolean isStartOfSentence() {
		return text != null && startOfSentence.matcher(text).find();
	}

	public boolean isUpperCase() {
		return language != null && text != null && text.toUpperCase(language.getLocale()).equals(text);
	}

	public boolean nextIsPunctuation() {
		return text != null && !text.isEmpty() && nextIsPunctuation.matcher(text).find();
	}

	public static boolean previousIsLetter(String str) {
		return str != null && previousIsLetter.matcher(str).find();
	}

	public boolean startsWithWhitespace() {
		return text != null && !text.isEmpty() && Character.isWhitespace(text.charAt(0));
	}

	public boolean startsWithNumber() {
		return text != null && !text.isEmpty() && Character.isDigit(text.charAt(0));
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

	public static String unixTimestampToISODate(long timestamp) {
		if (timestamp < 0) {
			return "--";
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		sdf.setTimeZone(TimeZone.getDefault());

		return sdf.format(new Date(timestamp));
	}

	@NonNull
	@Override
	public String toString() {
		return text == null ? "" : text;
	}
}
