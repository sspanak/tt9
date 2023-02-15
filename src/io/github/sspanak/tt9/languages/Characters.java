package io.github.sspanak.tt9.languages;

import android.graphics.Paint;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;

public class Characters {
	final public static ArrayList<String> Sentence = new ArrayList<>(Arrays.asList(
		",", ".", "-", "(", ")", "[", "]", "&", "~", "`", "'", ";", ":", "\"", "!", "?"
	));

	final public static ArrayList<String> Special = new ArrayList<>(Arrays.asList(
		" ", "\n", "@", "_", "#", "%", "$", "{", "}", "|", "^", "<", ">", "\\", "/", "=", "*", "+"
	));

	final private static ArrayList<String> TextEmoticons = new ArrayList<>(Arrays.asList(
		":)", ":D", ":P", ";)", "\\m/", ":-O", ":|", ":("
	));

	final private static ArrayList<ArrayList<String>> Emoji = new ArrayList<>(Arrays.asList(
		// smile -> frown
		new ArrayList<>(Arrays.asList(
			"🙂", "😀", "🤣", "😉", "😛", "😳", "😲", "😱", "😭", "😢", "🙁"
		)),
		// hands
		new ArrayList<>(Arrays.asList(
			"👍", "👋", "✌️", "👏", "🤝", "💪", "🤘", "🖖", "👎"
		)),
		// emotions
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
