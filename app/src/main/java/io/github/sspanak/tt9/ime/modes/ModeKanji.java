package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ModeKanji extends ModePinyin {
	private final String NAME = super.toString().replace(" / ローマ字", "");

	protected ModeKanji(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
	}

	@Override
	public boolean changeLanguage(@Nullable Language newLanguage) {
		if (LanguageKind.isJapanese(newLanguage)) {
			setLanguage(newLanguage);
			return true;
		}

		return false;
	}

	@NonNull
	@Override
	public String toString() {
		return NAME;
	}
}
