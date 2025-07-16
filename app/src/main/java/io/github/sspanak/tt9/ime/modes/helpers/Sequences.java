package io.github.sspanak.tt9.ime.modes.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;

public class Sequences {
	public static final int CHARS_0_KEY = 0;
	public static final int CHARS_0_CODE = CHARS_0_KEY + '0';
	public static final int CHARS_1_KEY = 1;
	public static final int CUSTOM_EMOJI_KEY = 3;

	public final int PUNCTUATION_PREFIX_LENGTH;

	@NonNull public final String CHARS_1_SEQUENCE;
	@NonNull public final String EMOJI_SEQUENCE;
	@NonNull public final String CUSTOM_EMOJI_SEQUENCE;
	@NonNull public final String CHARS_GROUP_1_SEQUENCE;

	@NonNull public final String CHARS_0_SEQUENCE;
	@NonNull public final String PREFERRED_CHAR_SEQUENCE;
	@NonNull public final String CHARS_GROUP_0_SEQUENCE;

	public Sequences() {
		this(null, null);
	}

	public Sequences(@Nullable String chars1Prefix, @Nullable String chars0Prefix) {
		final String CHARS_1_PREFIX = chars1Prefix != null ? chars1Prefix : "";
		final String CHARS_0_PREFIX = chars0Prefix != null ? chars0Prefix : "";

		CHARS_1_SEQUENCE = CHARS_1_PREFIX + CHARS_1_KEY;
		PUNCTUATION_PREFIX_LENGTH = CHARS_1_PREFIX.length();

		EMOJI_SEQUENCE = CHARS_1_SEQUENCE + CHARS_1_KEY;
		CUSTOM_EMOJI_SEQUENCE = EMOJI_SEQUENCE + CUSTOM_EMOJI_KEY;
		CHARS_GROUP_1_SEQUENCE = CHARS_1_SEQUENCE + 'G' + CHARS_1_KEY;

		CHARS_0_SEQUENCE = CHARS_0_PREFIX + CHARS_0_KEY;
		PREFERRED_CHAR_SEQUENCE = CHARS_0_SEQUENCE + CHARS_0_KEY;
		CHARS_GROUP_0_SEQUENCE = CHARS_0_SEQUENCE + 'G' + CHARS_0_KEY;
	}

	public boolean startsWithEmojiSequence(String sequence) {
		return
			sequence != null
			&& (sequence.startsWith(EMOJI_SEQUENCE) || sequence.startsWith(CUSTOM_EMOJI_SEQUENCE));
	}

	public boolean isAnySpecialCharSequence(String sequence) {
		if (sequence == null) {
			return false;
		}

		return
			sequence.equals(CHARS_1_SEQUENCE)
			|| sequence.equals(CHARS_0_SEQUENCE)
			|| sequence.equals(EMOJI_SEQUENCE)
			|| sequence.equals(PREFERRED_CHAR_SEQUENCE)
			|| sequence.equals(CHARS_GROUP_0_SEQUENCE)
			|| sequence.equals(CHARS_GROUP_1_SEQUENCE);
	}

	public boolean isEnglishI(@Nullable Language language, @NonNull String digitSequence) {
		return LanguageKind.isEnglish(language) && digitSequence.equals("4");
	}
}
