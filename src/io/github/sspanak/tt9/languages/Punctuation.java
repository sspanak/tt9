package io.github.sspanak.tt9.languages;

import android.graphics.Paint;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;

public class Punctuation {
	final public static ArrayList<String> Main = new ArrayList<>(Arrays.asList(
		",", ".", "-", "_", "(", ")", "[", "]", "&", "~", "`", "'", ";", ":", "\"", "!", "?"
	));

	final public static ArrayList<String> Secondary = new ArrayList<>(Arrays.asList(
		" ", "\n", "@", "%", "#", "$", "{", "}", "^", "<", ">", "\\", "/", "=", "*", "+"
	));

	final private static ArrayList<String> TextEmoticons = new ArrayList<>(Arrays.asList(
		":)", ":D", ":P", ";)", "\\m/", ":-O", ":|", ":("
	));

	final private static ArrayList<ArrayList<String>> Emoji = new ArrayList<>(Arrays.asList(
		new ArrayList<>(Arrays.asList(
			"🙂", "😀", "🤣", "😉", "😛", "😳", "😲", "😱", "😭", "😢", "🙁"
		)),
		new ArrayList<>(Arrays.asList(
			"👍", "👋", "✌️", "👏", "🤝", "💪", "🤘", "🖖", "👎"
		)),
		new ArrayList<>(Arrays.asList(
			"❤", "🤗", "😍", "😘", "😇", "😈", "🎉", "🤓", "😎", "🤔", "🥶", "😬"
		))
	));


	public static int getEmojiLevels() {
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) ? 1 : Emoji.size();
	}


	public static ArrayList<String> getEmoji(int level) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return TextEmoticons;
		}

		level = (Emoji.size() > level) ? level : Emoji.size() - 1;

		Paint paint = new Paint();
		ArrayList<String> availableEmoji = new ArrayList<>();
		for (String emoji : Emoji.get(level)) {
			if (paint.hasGlyph(emoji)) {
				availableEmoji.add(emoji);
			}
		}

		return availableEmoji.size() > 0 ? availableEmoji : TextEmoticons;
	}
}
