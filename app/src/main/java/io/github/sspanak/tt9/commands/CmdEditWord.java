package io.github.sspanak.tt9.commands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.ime.modes.ModeRecomposing;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Logger;

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
			&& !tt9.shouldBeOff()
			&& (InputModeKind.isPredictive(tt9.getInputMode()) || InputModeKind.isABC(tt9.getInputMode()))
			&& tt9.getLanguage() != null
			&& !tt9.getLanguage().isTranscribed();
	}


	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || !validate(tt9, tt9.getLanguage())) {
			return false;
		}

		final int previousMode = tt9.getInputMode().getId();
		if (previousMode == InputMode.MODE_RECOMPOSING) {
			Logger.d(getClass().getSimpleName(), "Already in recomposing mode. Nothing to do.");
			return true;
		}

		String word = tt9.getSuggestionOps().getCurrent(tt9.getLanguage(), tt9.getInputMode().getSequenceLength());
		if (word.isEmpty()) {
			word = tt9.getTextField().recomposeSurroundingWord(tt9.getLanguage());
		} else {
			tt9.getSuggestionOps().set(null);
		}

		if (word.isEmpty()) {
			UI.toastShortSingle(tt9, R.string.edit_word_no_selection);
			return true;
		}

		tt9.setInputMode(InputMode.MODE_RECOMPOSING);
		if (tt9.getInputMode().setWordStem(word, false)) {
			((ModeRecomposing) tt9.getInputMode()).setOnFinishListener(() -> tt9.setInputMode(previousMode));
			tt9.getSuggestions(0, "", null);
		} else {
			tt9.getTextField().finishComposingText();
			tt9.setInputMode(previousMode);
			String languageName = tt9.getLanguage() != null ? tt9.getLanguage().getName() : "";
			UI.toastShortSingle(
				tt9,
				"edit_word_invalid_characters",
				tt9.getString(R.string.edit_word_invalid_characters, word, languageName)
			);
		}
		return true;
	}


	private boolean validate(@NonNull TraditionalT9 tt9, @Nullable Language language) {
		if (tt9.isVoiceInputActive()) {
			return false;
		}

		if (language == null || language.isTranscribed()) {
			UI.toastShortSingle(tt9, R.string.edit_word_not_available_in_language);
			return false;
		}

		if (DictionaryLoader.isRunning()) {
			UI.toastShortSingle(tt9, R.string.dictionary_loading_please_wait);
			return false;
		}

		return true;
	}
}
