package io.github.sspanak.tt9.commands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.LanguageKind;

public class CmdSuggestionNext implements Command {
	public static final String ID = "key_next_suggestion";

	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_dpad_right; }
	public int getName() { return R.string.function_next_suggestion; }


	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return tt9 != null && !tt9.getSuggestionOps().isEmpty();
	}


	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || tt9.getSuggestionOps().isEmpty()) {
			return false;
		}

		final boolean backward = LanguageKind.isRTL(tt9.getLanguage());
		scrollSuggestions(tt9, backward);

		if (tt9.getMainView() != null) {
			tt9.getMainView().renderDynamicKeys();
		}

		return true;
	}


	public static void scrollSuggestions(@NonNull TraditionalT9 tt9, boolean backward) {
		tt9.getSuggestionOps().cancelDelayedAccept();
		tt9.getSuggestionOps().scrollTo(backward ? -1 : 1);
		tt9.getInputMode().setWordStem(tt9.getSuggestionOps().getCurrent(), true);
		if (InputModeKind.isRecomposing(tt9.getInputMode())) {
			tt9.getAppHacks().setComposingTextPartsWithHighlightedJoining(tt9.getInputMode().getWordStem() + tt9.getSuggestionOps().getCurrent(), tt9.getInputMode().getRecomposingSuffix());
		} else {
			tt9.getAppHacks().setComposingTextWithHighlightedStem(tt9.getSuggestionOps().getCurrent(), tt9.getInputMode().getWordStem(), tt9.getInputMode().isStemFilterFuzzy());
		}
	}
}
