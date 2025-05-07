package io.github.sspanak.tt9.ime.modes.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Sequences {
	public static final int SPECIAL_CHAR_KEY = 0;
	public static final int SPECIAL_CHAR_CODE = SPECIAL_CHAR_KEY + '0';
	public static final int PUNCTUATION_KEY = 1;
	public static final int CUSTOM_EMOJI_KEY = 3;

	public final int PUNCTUATION_PREFIX_LENGTH;

	@NonNull public final String PUNCTUATION_SEQUENCE;
	@NonNull public final String EMOJI_SEQUENCE;
	@NonNull public final String CUSTOM_EMOJI_SEQUENCE;

	@NonNull public final String CURRENCY_SEQUENCE;
	@NonNull public final String PREFERRED_CHAR_SEQUENCE;
	@NonNull public final String SPECIAL_CHAR_SEQUENCE;
	@NonNull public final String WHITESPACE_SEQUENCE;

	public Sequences() {
		this(null, null);
	}

	public Sequences(@Nullable String punctuationPrefix, @Nullable String specialCharPrefix) {
		final String PUNCTUATION_PREFIX = punctuationPrefix != null ? punctuationPrefix : "";
		final String SPECIAL_CHAR_PREFIX = specialCharPrefix != null ? specialCharPrefix : "";

		PUNCTUATION_SEQUENCE = PUNCTUATION_PREFIX + PUNCTUATION_KEY;
		PUNCTUATION_PREFIX_LENGTH = PUNCTUATION_PREFIX.length();

		EMOJI_SEQUENCE = PUNCTUATION_SEQUENCE + PUNCTUATION_KEY;
		CUSTOM_EMOJI_SEQUENCE = EMOJI_SEQUENCE + CUSTOM_EMOJI_KEY;

		WHITESPACE_SEQUENCE = SPECIAL_CHAR_PREFIX + SPECIAL_CHAR_KEY;
		PREFERRED_CHAR_SEQUENCE = WHITESPACE_SEQUENCE + SPECIAL_CHAR_KEY;
		SPECIAL_CHAR_SEQUENCE = SPECIAL_CHAR_PREFIX + SPECIAL_CHAR_KEY + SPECIAL_CHAR_KEY + SPECIAL_CHAR_KEY;
		CURRENCY_SEQUENCE = SPECIAL_CHAR_SEQUENCE + SPECIAL_CHAR_KEY;
	}

	public boolean isAnySpecialCharSequence(String sequence) {
		if (sequence == null) {
			return false;
		}

		return
			sequence.equals(PUNCTUATION_SEQUENCE)
			|| sequence.equals(WHITESPACE_SEQUENCE)
			|| sequence.equals(EMOJI_SEQUENCE)
			|| sequence.equals(PREFERRED_CHAR_SEQUENCE)
			|| sequence.equals(SPECIAL_CHAR_SEQUENCE)
			|| sequence.equals(CURRENCY_SEQUENCE);
	}

	public boolean startsWithAnySpecialCharSequence(String sequence) {
		if (sequence == null) {
			return false;
		}

		return
			sequence.startsWith(PUNCTUATION_SEQUENCE)
				|| sequence.startsWith(WHITESPACE_SEQUENCE)
				|| sequence.startsWith(EMOJI_SEQUENCE)
				|| sequence.startsWith(PREFERRED_CHAR_SEQUENCE)
				|| sequence.startsWith(SPECIAL_CHAR_SEQUENCE)
				|| sequence.startsWith(CURRENCY_SEQUENCE);
	}
}
