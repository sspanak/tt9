package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.EmojiLanguage;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

public class ModeBopomofo extends ModePinyin {
	private static final String SPECIAL_CHAR_SEQUENCE_PREFIX = "S0";
	private static final String PUNCTUATION_SEQUENCE_PREFIX = "S1";

	protected ModeBopomofo(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
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
		KEY_CHARACTERS.add(TextTools.removeLettersFromList(applyPunctuationOrder(Characters.getSpecial(language), 0)));
		KEY_CHARACTERS.get(0).add(0, "0");

		// punctuation
		KEY_CHARACTERS.add(
			TextTools.removeLettersFromList(applyPunctuationOrder(Characters.PunctuationChinese, 1))
		);
	}


	protected void setSpecialCharacterConstants() {
		CUSTOM_EMOJI_SEQUENCE = PUNCTUATION_SEQUENCE_PREFIX + EmojiLanguage.CUSTOM_EMOJI_SEQUENCE;
		EMOJI_SEQUENCE = PUNCTUATION_SEQUENCE_PREFIX + EmojiLanguage.EMOJI_SEQUENCE;
		PUNCTUATION_SEQUENCE = PUNCTUATION_SEQUENCE_PREFIX + NaturalLanguage.PUNCTUATION_KEY;
		SPECIAL_CHAR_SEQUENCE = SPECIAL_CHAR_SEQUENCE_PREFIX + NaturalLanguage.SPECIAL_CHAR_KEY;
	}


	/***************************** TYPING *********************************/

	@Override
	public boolean onBackspace() {
		if (digitSequence.equals(PUNCTUATION_SEQUENCE) || digitSequence.equals(SPECIAL_CHAR_SEQUENCE)) {
			digitSequence = "";
			return false;
		} else {
			return super.onBackspace();
		}
	}


	@Override
	protected void onNumberPress(int nextNumber) {
		if (digitSequence.startsWith(PUNCTUATION_SEQUENCE)) {
			digitSequence = PUNCTUATION_SEQUENCE_PREFIX + EmojiLanguage.validateEmojiSequence(digitSequence.substring(PUNCTUATION_SEQUENCE_PREFIX.length()), nextNumber);
		} else {
			digitSequence += String.valueOf(nextNumber);
		}
	}


	@Override
	protected void onNumberHold(int number) {
		if (number == 0) {
			disablePredictions = false;
			digitSequence = SPECIAL_CHAR_SEQUENCE;
		} else if (number == 1) {
			disablePredictions = false;
			digitSequence = PUNCTUATION_SEQUENCE;
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
			|| newSequence.startsWith(SPECIAL_CHAR_SEQUENCE)
			|| (newSequence.startsWith(PUNCTUATION_SEQUENCE) && nextKey != NaturalLanguage.PUNCTUATION_KEY.charAt(0) - '0');
	}
}
