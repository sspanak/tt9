package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

import io.github.sspanak.tt9.languages.Characters;

public class Mode123 extends ModePassthrough {
	@Override public int getId() { return MODE_123; }
	@Override @NonNull public String toString() { return "123"; }

	@Override public final boolean is123() { return true; }
	@Override public boolean isPassthrough() { return false; }
	@Override public int getSequenceLength() { return 1; }
	@Override public boolean shouldAcceptPreviousSuggestion(int nextKey) { return true; }

	private final ArrayList<ArrayList<String>> KEY_CHARACTERS = new ArrayList<>();

	public Mode123() {
		// 0-key
		KEY_CHARACTERS.add(new ArrayList<>(Collections.singletonList("+")));
		for (String character : Characters.Special) {
			if (!character.equals("+") && !character.equals("\n")) {
				KEY_CHARACTERS.get(0).add(character);
			}
		}

		// 1-key
		KEY_CHARACTERS.add(new ArrayList<>(Collections.singletonList(".")));
		for (String character : Characters.PunctuationEnglish) {
			if (!character.equals(".")) {
				KEY_CHARACTERS.get(1).add(character);
			}
		}
	}

	@Override public boolean onNumber(int number, boolean hold, int repeat) {
		reset();

		if (hold && number < KEY_CHARACTERS.size()) {
			suggestions.addAll(KEY_CHARACTERS.get(number));
		} else {
			autoAcceptTimeout = 0;
			suggestions.add(String.valueOf(number));
		}

		return true;
	}

	/**
	 * shouldIgnoreText
	 * Since this is a numeric mode, we allow typing only numbers and:
	 * 	1. In numeric fields, we must allow math chars
	 * 	2. In dialer fields, we must allow various punctuation chars, because they are used as dialing shortcuts
	 * 	at least in Japan.
	 * More info and discussion: <a href="https://github.com/sspanak/tt9/issues/241">issue 241 on Github</a>.
	 */
	@Override public boolean shouldIgnoreText(String text) {
		return
			text == null
			|| text.length() != 1
			|| !(
				(text.charAt(0) > 31 && text.charAt(0) < 65)
				|| (text.charAt(0) > 90 && text.charAt(0) < 97)
				|| (text.charAt(0) > 122 && text.charAt(0) < 127)
			);
	}
}
