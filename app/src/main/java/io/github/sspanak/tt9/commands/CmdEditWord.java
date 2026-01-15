package io.github.sspanak.tt9.commands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.UI;

public class CmdEditWord implements Command {
	public static final String ID = "key_edit_word";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return R.drawable.ic_fn_edit_word; }
	@Override public int getName() { return R.string.function_edit_word; }
	@Override public int getHardKey() { return 2; }
	@Override public int getPaletteKey() { return R.id.soft_key_2; }


	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& tt9.getSettings().getPredictiveMode()
			&& tt9.getLanguage() != null
			&& !tt9.getLanguage().isTranscribed();
	}


	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		return tt9 != null && tt9.onKeyEditWord(false);
	}


	public static boolean validate(@NonNull TraditionalT9 tt9, @NonNull SettingsStore settings, @Nullable Language language) {
		if (tt9.isVoiceInputActive() || !settings.getPredictiveMode()) {
			return false;
		}

		if (language == null || language.isTranscribed()) {
			UI.toastShortSingle(tt9, R.string.edit_word_not_available_in_language);
			return false;
		}

		if (DictionaryLoader.getInstance(tt9).isRunning()) {
			UI.toastShortSingle(tt9, R.string.dictionary_loading_please_wait);
			return false;
		}

		return true;
	}
}
