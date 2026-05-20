package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.LanguageKind;

public class CmdSuggestionPrevious implements Command {
	public static final String ID = "key_previous_suggestion";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_dpad_left; }
	public int getName() { return R.string.function_previous_suggestion; }


	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return tt9 != null && !tt9.getSuggestionOps().isEmpty();
	}


	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || tt9.getSuggestionOps().isEmpty()) {
			return false;
		}

		final boolean backward = !LanguageKind.isRTL(tt9.getLanguage());
		CmdSuggestionNext.scrollSuggestions(tt9, backward);

		if (tt9.getMainView() != null) {
			tt9.getMainView().renderDynamicKeys();
		}

		return true;
	}
}
