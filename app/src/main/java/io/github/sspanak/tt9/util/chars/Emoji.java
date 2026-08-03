package io.github.sspanak.tt9.util.chars;

import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Arrays;

class Emoji extends Punctuation {
	final private static ArrayList<String> TextEmoticons = new ArrayList<>(Arrays.asList(
		":)", ":D", ":P", ";)", "\\m/", ":-O", ":|", ":("
	));

	final private static ArrayList<ArrayList<String>> Emoji = new ArrayList<>(Arrays.asList(
		// positive
		new ArrayList<>(Arrays.asList(
		  "🙂", "😊", "😁", "😆", "😅", "🤣", "😛", "😉", "🤓", "😎"
		)),
		// negative
		new ArrayList<>(Arrays.asList(
			"🙁", "😟", "😢", "😭", "😧", "😱", "😲", "😳", "😠", "😐"
		)),
		// hands
		new ArrayList<>(Arrays.asList(
			"👍", "👎", "👋", "👏", "🤞", "✌️", "💪", "🤝", "🤘", "🖖"
		)),
		// love
		new ArrayList<>(Arrays.asList(
			"❤", "🤗", "😘", "😍", "🤩", "🫂", "🫶", "❤️‍🔥", "💗", "💋"
		))
		// misc
		new ArrayList<>(Arrays.asList(
			"😬", "🥺", "😇", "😈", "🥱", "🤔", "🥶", "🥵", "🔥", "🎉"
		))
	));

	public static boolean isGraphic(char ch) {
		return !(ch < 256 || Character.isLetterOrDigit(ch) || Character.isAlphabetic(ch));
	}

	public static boolean isGraphic(int codePoint) {
		return !(codePoint < 256 || Character.isLetterOrDigit(codePoint) || Character.isAlphabetic(codePoint));
	}

	public static ArrayList<String> getEmoji(int level) {
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

	public static boolean isBuiltInEmoji(String emoji) {
		for (ArrayList<String> group : Emoji) {
			if (group.contains(emoji)) {
				return true;
			}
		}

		return false;
	}
}
