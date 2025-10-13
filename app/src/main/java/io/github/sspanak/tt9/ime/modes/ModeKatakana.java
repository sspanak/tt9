package io.github.sspanak.tt9.ime.modes;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.predictions.KanaPredictions;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ModeKatakana extends ModeHiragana {
	protected ModeKatakana(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
		NAME = "カタカナ";
	}

	@Override
	protected void initPredictions() {
		predictions = new KanaPredictions(settings, textField, true);
		predictions.setWordsChangedHandler(this::onPredictions);
	}

	@Override
	public int getId() {
		return MODE_KATAKANA;
	}
}
