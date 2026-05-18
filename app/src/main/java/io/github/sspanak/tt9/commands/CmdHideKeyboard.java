package io.github.sspanak.tt9.commands;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdHideKeyboard implements Command {
	public static final String ID = "key_hide_keyboard";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_fn_hide_keyboard; }
	public int getName() { return R.string.function_hide_keyboard; }

	public boolean run(TraditionalT9 tt9) {
		return tt9 != null && tt9.onKeyHideKeyboard(false);
	}
}
