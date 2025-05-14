package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.Sequences;
import io.github.sspanak.tt9.languages.EmojiLanguage;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

public class ModeBopomofo extends ModePinyin {

	protected ModeBopomofo(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
		seq = new Sequences("S1", "S0");
	}


	@Override
	public boolean changeLanguage(@Nullable Language newLanguage) {
		if (LanguageKind.isChineseBopomofo(newLanguage)) {
			setLanguage(newLanguage);
			return true;
		}

		return false;
	}


	/* **************************** LOAD SUGGESTIONS *********************************/

	/**
	 * Not possible in Bopomofo mode, because 0-key is used for typing letters.
	 */
	@Override protected boolean loadPreferredChar() { return false; }


	/**
	 * setCustomSpecialCharacters
	 * Filter out the letters from the 0-key list and add "0", because there is no other way of
	 * typing it.
	 */
	protected void setCustomSpecialCharacters() {
		// special
		KEY_CHARACTERS.add(TextTools.removeLettersFromList(Characters.orderByList(Characters.Special, settings.getOrderedKeyChars(language, 0), false)));
		KEY_CHARACTERS.get(0).add(0, "0");

		// punctuation
		KEY_CHARACTERS.add(
			TextTools.removeLettersFromList(Characters.orderByList(Characters.PunctuationChineseBopomofo, settings.getOrderedKeyChars(language, 1), false))
		);
	}

	/***************************** TYPING *********************************/

	@Override
	public boolean onBackspace() {
		if (digitSequence.equals(seq.CHARS_1_SEQUENCE) || digitSequence.equals(seq.CHARS_0_SEQUENCE)) {
			digitSequence = "";
			return false;
		} else {
			return super.onBackspace();
		}
	}


	@Override
	protected void onNumberPress(int nextNumber) {
		if (seq.startsWithEmojiSequence(digitSequence)) {
			digitSequence = EmojiLanguage.validateEmojiSequence(seq, digitSequence, nextNumber);
		} else if (!seq.CHARS_GROUP_0_SEQUENCE.equals(digitSequence) && !seq.CHARS_GROUP_1_SEQUENCE.equals(digitSequence)) {
			digitSequence += String.valueOf(nextNumber);
		}
	}


	@Override
	protected void onNumberHold(int number) {
		if (number == 0) {
			disablePredictions = false;
			digitSequence = seq.CHARS_0_SEQUENCE;
		} else if (number == 1) {
			disablePredictions = false;
			digitSequence = seq.CHARS_1_SEQUENCE;
		} else {
			autoAcceptTimeout = 0;
			suggestions.add(language.getKeyNumeral(number));
		}
	}


	/******************************* ACCEPT WORDS *********************************/

	/**
	 * In Bopomofo mode, the 0-key is not Spacebar, so we do not rely on the parents to handle accepting
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) {
		String newSequence = digitSequence + (char)(nextKey + '0');
		return hold
			|| newSequence.startsWith(seq.CHARS_0_SEQUENCE)
			|| (newSequence.startsWith(seq.CHARS_1_SEQUENCE) && nextKey != Sequences.CHARS_1_KEY);
	}
}
