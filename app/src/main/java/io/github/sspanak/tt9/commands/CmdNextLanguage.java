package io.github.sspanak.tt9.commands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.dialogs.ChangeLanguageDialog;

public class CmdNextLanguage implements Command {
	public static final String ID = "key_next_language";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_fn_next_language; }
	public int getName() { return R.string.function_next_language; }


	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& !tt9.shouldBeOff()
			&& tt9.getSettings().areEnabledLanguagesMoreThanN(1)
			&& !tt9.isInputModeNumeric();
	}


	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}

		tt9.getSuggestionOps().cancelDelayedAccept();
		tt9.stopVoiceInput();

		if (tt9.getSettings().getQuickSwitchLanguage() || !new ChangeLanguageDialog(tt9, tt9::setLang).show()) {
			ArrayList<Integer> enabledLanguages = tt9.getSettings().getEnabledLanguageIds();
			tt9.setLang(
				getNextLang(enabledLanguages, tt9.getLanguage() != null ? tt9.getLanguage().getId() : -1)
			);
		}

		return true;
	}


	private int getNextLang(@NonNull ArrayList<Integer> enabledLanguages, int currentLangId) {
		int previous = enabledLanguages.indexOf(currentLangId);
		int next = (previous + 1) % enabledLanguages.size();
		return enabledLanguages.get(next);
	}

}
