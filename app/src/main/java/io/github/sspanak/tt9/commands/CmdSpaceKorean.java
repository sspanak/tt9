package io.github.sspanak.tt9.commands;

import android.view.KeyEvent;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.chars.Characters;

public class CmdSpaceKorean implements Command {
	public static final String ID = "key_space_korean";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_fn_space; }
	public int getName() { return R.string.virtual_key_space_korean; }


	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		return runFromHotkey(tt9, false);
	}


	/**
	 * For CJK languages: when there are suggestions, accept the current one, otherwise type a space.
	 * For Non-CJK languages: accept the current suggestion (if any) AND type a space.
	 * The name is kept for historical reasons, because Korean was the first to introduce this behavior.
	 */
	@Override
	public boolean runFromHotkey(@Nullable TraditionalT9 tt9, boolean validateOnly) {
		if (tt9 == null) {
			return false;
		}

		// simulate accept with OK when there are suggestions
		if (!tt9.getSuggestionOps().isEmpty() && LanguageKind.isCJK(tt9.getLanguage())) {
			if (!validateOnly) {
				tt9.onAcceptSuggestionManually(tt9.getSuggestionOps().acceptCurrent(), KeyEvent.KEYCODE_ENTER);
			}
			return true;
		}

		// type a space when there is nothing to accept
		return tt9.onText(Characters.getSpace(tt9.getLanguage()), validateOnly);
	}
}
