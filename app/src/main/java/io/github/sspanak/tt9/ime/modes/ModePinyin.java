package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

public class ModePinyin extends ModeIdeograms {
	protected ModePinyin(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
	}


	@Override
	public boolean validateLanguage(@Nullable Language newLanguage) {
		return LanguageKind.isChinesePinyin(newLanguage);
	}


	@Override
	protected String getPreferredChar() {
		return Characters.getChar(language, settings.getDoubleZeroChar());
	}
}
