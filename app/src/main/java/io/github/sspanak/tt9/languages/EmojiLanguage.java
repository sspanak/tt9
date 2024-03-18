package io.github.sspanak.tt9.languages;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

public class EmojiLanguage extends Language {
	final public static String EMOJI_SEQUENCE = "11";
	final private static int CUSTOM_EMOJI_KEY = 3;
	final public static String CUSTOM_EMOJI_SEQUENCE = EMOJI_SEQUENCE + CUSTOM_EMOJI_KEY;

	public EmojiLanguage() {
		id = Integer.parseInt(EMOJI_SEQUENCE);
		locale = Locale.ROOT;
		abcString = "emoji";
		name = "Emoji";
	}

	@Override
	public String getDigitSequenceForWord(String word) {
		return TextTools.isGraphic(word) ? CUSTOM_EMOJI_SEQUENCE : null;
	}

	@Override
	public ArrayList<String> getKeyCharacters(int key, int characterGroup) {
		return key == 1 && characterGroup >= 0 ? new ArrayList<>(Characters.getEmoji(characterGroup)) : super.getKeyCharacters(key, characterGroup);
	}

	public static String validateEmojiSequence(@NonNull String sequence, int next) {
		if (sequence.startsWith(CUSTOM_EMOJI_SEQUENCE) || (sequence.equals(EMOJI_SEQUENCE) && next == CUSTOM_EMOJI_KEY)) {
			return CUSTOM_EMOJI_SEQUENCE;
		} else if (sequence.startsWith(EMOJI_SEQUENCE) && (next > 1 || sequence.length() == Characters.getMaxEmojiLevel() + 1)) {
			return sequence;
		} else {
			return sequence + next;
		}
	}
}
