package io.github.sspanak.tt9.commands;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdSelectKeyboard implements Command {
	public static final String ID = "key_select_keyboard";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return R.drawable.ic_fn_next_keyboard; }
	@Override public int getName() { return R.string.function_select_keyboard; }
	@Override public int getHardKey() { return 8; }
	@Override public int getPaletteKey() { return R.id.soft_key_8; }

	@Override public boolean run(TraditionalT9 tt9) {
		if (tt9 != null) {
			tt9.selectKeyboard();
			return true;
		}

		return false;
	}
}
