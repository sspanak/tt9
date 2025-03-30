package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

public class ModePinyin extends ModeIdeograms {
	boolean ignoreNextSpace = false;


	protected ModePinyin(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
	}


	@Override
	public boolean changeLanguage(@Nullable Language newLanguage) {
		return LanguageKind.isChinese(newLanguage) && super.changeLanguage(newLanguage);
	}


	@Override
	protected void onNumberPress(int number) {
		if (ignoreNextSpace && number == SPECIAL_CHAR_SEQUENCE.charAt(0) - '0') {
			ignoreNextSpace = false;
			return;
		}

		ignoreNextSpace = false;
		super.onNumberPress(number);
	}


	@Override
	protected void onNumberHold(int number) {
		ignoreNextSpace = false;
		super.onNumberHold(number);
	}


	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) {
		// In East Asian languages, 0-key must accept the current word, or type a space when there is no word.
		if (!digitSequence.isEmpty() && !digitSequence.endsWith(SPECIAL_CHAR_SEQUENCE) && nextKey == SPECIAL_CHAR_SEQUENCE.charAt(0) - '0') {
			ignoreNextSpace = true;
		}

		return super.shouldAcceptPreviousSuggestion(nextKey, hold);
	}


	@Override
	protected String getPreferredChar() {
		final String preferredChar = settings.getDoubleZeroChar();
		return switch (preferredChar) {
			case "." -> Characters.ZH_FULL_STOP;
			case "," -> Characters.ZH_COMMA_LIST;
			default -> preferredChar;
		};
	}
}
