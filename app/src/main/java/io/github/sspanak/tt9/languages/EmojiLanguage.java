package io.github.sspanak.tt9.languages;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import io.github.sspanak.tt9.ime.modes.helpers.Sequences;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

public class EmojiLanguage extends Language {
	private final Sequences seq;

	public EmojiLanguage() {
		this(null);
	}

	public EmojiLanguage(Sequences sequences) {
		id = Integer.parseInt(new Sequences().EMOJI_SEQUENCE); // always use the unprefixed sequence for ID
		locale = Locale.ROOT;
		abcString = "emoji";
		code = "emj";
		currency = "";
		name = "Emoji";
		seq = sequences == null ? new Sequences() : sequences;
	}

	@NonNull
	@Override
	public String getDigitSequenceForWord(String word) {
		if (!isValidWord(word)) {
			return "";
		}

		return Characters.isBuiltInEmoji(word) ? seq.EMOJI_SEQUENCE : seq.CUSTOM_EMOJI_SEQUENCE;
	}

	@NonNull
	public ArrayList<String> getKeyCharacters(int key, int characterGroup) {
		return key == 1 && characterGroup >= 0 ? Characters.getEmoji(characterGroup) : new ArrayList<>();
	}

	@NonNull
	@Override
	public ArrayList<String> getKeyCharacters(int key) {
		return getKeyCharacters(key, 0);
	}

	@Override
	public boolean isValidWord(String word) {
		return TextTools.isGraphic(word);
	}

	public static String validateEmojiSequence(@NonNull Sequences seq, @NonNull String sequence, int next) {
		if (sequence.startsWith(seq.CUSTOM_EMOJI_SEQUENCE) || (sequence.equals(seq.EMOJI_SEQUENCE) && next == Sequences.CUSTOM_EMOJI_KEY)) {
			return seq.CUSTOM_EMOJI_SEQUENCE;
		} else if (sequence.startsWith(seq.EMOJI_SEQUENCE) && (next > 1 || sequence.length() - seq.PUNCTUATION_PREFIX_LENGTH == Characters.getMaxEmojiLevel() + 1)) {
			return sequence;
		} else {
			return sequence + next;
		}
	}
}
