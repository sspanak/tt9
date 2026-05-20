package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputMode;

public class CmdFilterClear implements Command {
	public static final String ID = "key_filter_clear";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_fn_filter_off; }
	public int getName() { return R.string.function_filter_clear; }

	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return tt9 != null && !tt9.getSuggestionOps().containsNoOrdinaryWords();
	}

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}

		tt9.getSuggestionOps().cancelDelayedAccept();

		// This achieves the "back to leave word as-is" behavior of Nokia 3310. As suggested by the
		// community, clearing the filter makes sense only when it is actually in effect. Otherwise,
		// it simply causes the current selection to be reset, which is confusing.
		// References:
		//  - https://github.com/sspanak/tt9/issues/698#issuecomment-2600441061
		//  - https://github.com/sspanak/tt9/issues/418
		InputMode inputMode = tt9.getInputMode();
		int stemLength = inputMode.getWordStem().length();
		boolean isFilteringOn = inputMode.isStemFilterFuzzy() || (stemLength > 0 && inputMode.getSequenceLength() != stemLength);

		if (inputMode.clearWordStem() && isFilteringOn) {
			inputMode
				.setOnSuggestionsUpdated(tt9::handleSuggestionsAsync)
				.loadSuggestions(tt9.getSuggestionOps().getCurrent(tt9.getLanguage(), inputMode.getSequenceLength()));
			return true;
		}

		inputMode.onAcceptSuggestion(tt9.getSuggestionOps().acceptIncomplete());
		tt9.resetKeyRepeat();

		if (tt9.getMainView() != null) {
			tt9.getMainView().renderDynamicKeys();
		}

		return true;
	}
}
