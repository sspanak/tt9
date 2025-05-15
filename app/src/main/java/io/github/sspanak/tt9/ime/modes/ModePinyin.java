package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.Sequences;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

public class ModePinyin extends ModeIdeograms {
	private boolean ignoreNextSpace = false;


	protected ModePinyin(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
	}


	@Override
	public boolean validateLanguage(@Nullable Language newLanguage) {
		return LanguageKind.isChinesePinyin(newLanguage);
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


	@Override
	protected void onNumberPress(int number) {
		if (ignoreNextSpace && number == Sequences.CHARS_0_KEY) {
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
		// In East Asian languages, Space must accept the current word, or type a space when there is no word.
		// Here, we handle the case when 0-key is Space, unlike the Space hotkey in HotkeyHandler,
		// which could be a different key, assigned by the user.
		if (!digitSequence.isEmpty() && !digitSequence.equals(seq.CHARS_0_SEQUENCE) && nextKey == Sequences.CHARS_0_KEY) {
			ignoreNextSpace = true;
		}

		return super.shouldAcceptPreviousSuggestion(nextKey, hold);
	}
}
