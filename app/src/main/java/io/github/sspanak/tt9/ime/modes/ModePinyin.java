package io.github.sspanak.tt9.ime.modes;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

public class ModePinyin extends ModeIdeograms {

	protected ModePinyin(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
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
