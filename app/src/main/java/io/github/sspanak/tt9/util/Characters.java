package io.github.sspanak.tt9.util;

import android.graphics.Paint;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;

public class Characters {
	public static final String GR_QUESTION_MARK = ";";
	public static final String NEW_LINE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && new Paint().hasGlyph("⏎") ? "⏎" : "\\n";

	final public static ArrayList<String> ArabicNumbers = new ArrayList<>(Arrays.asList(
		"٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩"
	));

	final public static ArrayList<Character> CombiningPunctuation = new ArrayList<>(Arrays.asList(
		',', '-', '\'', ':', ';', '!', '?', '.'
	));

	final public static ArrayList<Character> CombiningPunctuationHebrew = new ArrayList<>(Arrays.asList(
		',' , '-', '\'', ':', ';', '!', '?', '.', '"',
		'·', GR_QUESTION_MARK.charAt(0), // Greek
		'،', '؛', ':', '!', '؟' // Arabic
	));

	final public static ArrayList<String> PunctuationArabic = new ArrayList<>(Arrays.asList(
		"،", ".", "-", "(", ")", "&", "~", "`", "'", "\"",  "؛", ":", "!", "؟"
	));

	final public static ArrayList<String> PunctuationEnglish = new ArrayList<>(Arrays.asList(
		",", ".", "-", "(", ")", "&", "~", "`", ";", ":", "'", "\"", "!", "?"
	));

	final public static ArrayList<String> PunctuationFrench = new ArrayList<>(Arrays.asList(
		",", ".", "-", "«", "»", "(", ")", "&", "`", "~", ";", ":", "'", "\"", "!", "?"
	));

	final public static ArrayList<String> PunctuationGerman = new ArrayList<>(Arrays.asList(
		",", ".", "-", "„", "“", "(", ")", "&", "~", "`", "'", "\"", ";", ":", "!", "?"
	));

	final public static ArrayList<String> PunctuationGreek = new ArrayList<>(Arrays.asList(
		",", ".", "-", "«", "»", "(", ")", "&", "~", "`", "'", "\"", "·", ":", "!", GR_QUESTION_MARK
	));

	final public static ArrayList<String> Currency = new ArrayList<>(Arrays.asList(
		"$", "€", "₹", "₿", "₩", "¢", "¤", "₺", "₱", "¥", "₽", "£"
	));

	final public static ArrayList<String> Special = new ArrayList<>(Arrays.asList(
		" ", "\n", "@", "_", "#", "%", "[", "]", "{", "}", "§", "|", "^", "<", ">", "\\", "/", "=", "*", "+"
	));

	/**
	 * The English punctuation filtered to contain only valid email characters.
	 */
	final public static ArrayList<ArrayList<String>> Email = new ArrayList<>(Arrays.asList(
		new ArrayList<>(Arrays.asList("@", "_", "#", "%", "{", "}", "|", "^", "/", "=", "*", "+")),
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
	 * Special characters for all kinds of numeric fields: integer, decimal with +/- included as necessary.
	 */
	public static ArrayList<ArrayList<String>> getNumberSpecialCharacters(boolean decimal, boolean signed) {
		ArrayList<ArrayList<String>> keyCharacters = new ArrayList<>();
		keyCharacters.add(signed ? new ArrayList<>(Arrays.asList("-", "+")) : new ArrayList<>());
		if (decimal) {
			keyCharacters.add(new ArrayList<>(Arrays.asList(".", ",")));
		}
		return keyCharacters;
	}

	final private static ArrayList<String> TextEmoticons = new ArrayList<>(Arrays.asList(
		":)", ":D", ":P", ";)", "\\m/", ":-O", ":|", ":("
	));

	final private static ArrayList<ArrayList<String>> Emoji = new ArrayList<>(Arrays.asList(
		// positive
		new ArrayList<>(Arrays.asList(
			"🙂", "😀", "🤣", "🤓", "😎", "😛", "😉"
		)),
		// negative
		new ArrayList<>(Arrays.asList(
			"🙁", "😢", "😭", "😱", "😲", "😳", "😐", "😠"
		)),
		// hands
		new ArrayList<>(Arrays.asList(
			"👍", "👋", "✌️", "👏", "🖖", "🤘", "🤝", "💪", "👎"
		)),
		// emotions
		new ArrayList<>(Arrays.asList(
			"❤", "🤗", "😍", "😘", "😇", "😈", "🍺", "🎉", "🥱", "🤔", "🥶", "😬"
		))
	));

	public static boolean isStaticEmoji(String emoji) {
		for (ArrayList<String> group : Emoji) {
			if (group.contains(emoji)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isGraphic(char ch) {
		return !(ch < 256 || Character.isLetterOrDigit(ch) || Character.isAlphabetic(ch));
	}

	public static boolean noEmojiSupported() {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
	}

	public static ArrayList<String> getEmoji(int level) {
		if (noEmojiSupported()) {
			return new ArrayList<>(TextEmoticons);
		}

		if (level < 0 || level >= Emoji.size()) {
			return new ArrayList<>();
		}

		Paint paint = new Paint();
		ArrayList<String> availableEmoji = new ArrayList<>();
		for (String emoji : Emoji.get(level)) {
			if (paint.hasGlyph(emoji)) {
				availableEmoji.add(emoji);
			}
		}

		return availableEmoji.isEmpty() ? new ArrayList<>(TextEmoticons) : availableEmoji;
	}

	public static int getMaxEmojiLevel() {
		return Emoji.size();
	}

	public static boolean isCombiningPunctuation(Language language, char ch) {
		return
			CombiningPunctuation.contains(ch)
			|| (LanguageKind.isHebrew(language) && CombiningPunctuationHebrew.contains(ch));
	}
}
