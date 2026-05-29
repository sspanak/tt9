package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.UI;

public class CmdFilterSuggestions implements Command {
	public static final String ID = "key_filter_suggestions";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_fn_filter; }
	public int getName() { return R.string.function_filter_suggestions; }


	public int getDynamicIcon(@Nullable TraditionalT9 tt9) {
		if (tt9 != null) {
			if (tt9.isFilteringFuzzy()) return R.drawable.ic_fn_filter_fuzzy;
			if (tt9.isFilteringOn()) return R.drawable.ic_fn_filter_exact;
		}
		return getIcon();
	}


	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& tt9.getInputMode().supportsFiltering()
			&& isAvailableFromHotkey(tt9);
	}


	private boolean isAvailableFromHotkey(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& !tt9.isVoiceInputActive()
			&& !tt9.isFnPanelVisible()
			&& !tt9.getSuggestionOps().containsNoOrdinaryWords();
	}


	public boolean run(@Nullable TraditionalT9 tt9, boolean repeat) {
		if (tt9 == null) {
			return false;
		}

		if (!tt9.getInputMode().supportsFiltering()) {
			UI.toastShortSingle(tt9, R.string.function_filter_suggestions_not_available);
			return true; // prevent the default key action to acknowledge we have processed the event
		}

		tt9.getSuggestionOps().cancelDelayedAccept();

		String filter;
		if (repeat && !tt9.getSuggestionOps().get(1).isEmpty()) {
			filter = tt9.getSuggestionOps().get(1);
		} else {
			filter = tt9.getSuggestionOps().getCurrent(tt9.getLanguage(), tt9.getInputMode().getSequenceLength());
		}

		if (filter.isEmpty()) {
			tt9.getInputMode().reset();
		} else if (tt9.getInputMode().setWordStem(filter, repeat)) {
			tt9.getInputMode()
				.setOnSuggestionsUpdated(tt9::handleSuggestionsAsync)
				.loadSuggestions(filter);
		}

		if (tt9.getMainView() != null) {
			tt9.getMainView().renderDynamicKeys();
		}

		return true;
	}


	public boolean runFromHotkey(@Nullable TraditionalT9 tt9, boolean validateOnly, boolean repeat) {
		return isAvailableFromHotkey(tt9) && (validateOnly || run(tt9, repeat));
	}
}
