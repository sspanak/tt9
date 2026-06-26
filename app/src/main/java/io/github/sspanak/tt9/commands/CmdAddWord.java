package io.github.sspanak.tt9.commands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.dialogs.AddWordDialog;

public class CmdAddWord implements Command {
	public static final String ID = "key_add_word";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return R.drawable.ic_fn_add_word; }
	@Override public int getName() { return R.string.function_add_word; }
	@Override public int getHardKey() { return 1; }
	@Override public int getPaletteKey() { return R.id.soft_key_1; }


	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& tt9.getSettings().getPredictiveMode()
			&& !tt9.shouldBeOff()
			&& (InputModeKind.isPredictive(tt9.getInputMode()) || InputModeKind.isABC(tt9.getInputMode()))
			&& tt9.getLanguage() != null
			&& !tt9.getLanguage().isTranscribed();
	}


	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || tt9.getMainView() == null || !validate(tt9, tt9.getLanguage()) || tt9.getLanguage() == null) {
			return false;
		}

		tt9.getSuggestionOps().cancelDelayedAccept();
		tt9.getInputMode().onAcceptSuggestion(tt9.getSuggestionOps().acceptIncomplete());
		tt9.getMainView().showKeyboard();
		tt9.resetStatus();

		new AddWordDialog(
			tt9,
			tt9.getLanguage(),
			tt9.getTextField().getSurroundingWord(tt9.getLanguage())
		).show();

		return true;
	}


	private boolean validate(@NonNull TraditionalT9 tt9, @Nullable Language language) {
		if (tt9.isVoiceInputActive()) {
			return false;
		}

		if (language == null || language.isTranscribed()) {
			UI.toastShortSingle(tt9, R.string.add_word_not_available_in_language);
			return false;
		}

		if (DictionaryLoader.isRunning()) {
			UI.toastShortSingle(tt9, R.string.dictionary_loading_please_wait);
			return false;
		}

		return true;
	}
}
