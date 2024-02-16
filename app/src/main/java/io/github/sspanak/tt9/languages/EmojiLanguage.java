package io.github.sspanak.tt9.languages;

import java.util.ArrayList;
import java.util.Locale;

public class EmojiLanguage extends Language {
	final public static String EMOJI_SEQUENCE = "11";
	final public static String CUSTOM_EMOJI_SEQUENCE = EMOJI_SEQUENCE + "1";

	public EmojiLanguage() {
		id = Integer.parseInt(EMOJI_SEQUENCE);
		locale = Locale.ROOT;
		abcString = "emoji";
		name = "Emoji";
	}

	@Override
	public String getDigitSequenceForWord(String word) {
		return Characters.isGraphic(word) ? CUSTOM_EMOJI_SEQUENCE : null;
	}

	@Override
	public ArrayList<String> getKeyCharacters(int key, int characterGroup) {
		return key == 1 && characterGroup >= 0 ? new ArrayList<>(Characters.getEmoji(characterGroup)) : super.getKeyCharacters(key, characterGroup);
	}
}
