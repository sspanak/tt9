package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.predictions.KanaPredictions;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ModeHiragana extends ModeKanji {
	protected ModeHiragana(@NonNull SettingsStore settings, @NonNull Language lang, @Nullable InputType inputType, @Nullable TextField textField) {
		super(settings, lang, inputType, textField);
		NAME = "ひらがな";
	}

	@Override
	protected void initPredictions() {
		predictions = new KanaPredictions(settings, false);
		predictions.setWordsChangedHandler(this::onPredictions);
	}

	@Override
	public int getId() {
		return MODE_HIRAGANA;
	}
}
