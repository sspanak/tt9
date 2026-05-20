package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdHideKeyboard implements Command {
	public static final String ID = "key_hide_keyboard";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_fn_hide_keyboard; }
	public int getName() { return R.string.function_hide_keyboard; }

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}

		tt9.getSuggestionOps().cancelDelayedAccept();
		tt9.getSuggestionOps().acceptIncomplete();
		tt9.getInputMode().reset();

		tt9.requestHideSelf(0);

		return true;
	}
}
