package io.github.sspanak.tt9.commands;

import io.github.sspanak.tt9.R;

public class CmdFilterClear implements Command {
	public static final String ID = "key_filter_clear";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_fn_filter_off; }
	public int getName() { return R.string.function_filter_clear; }

	public boolean run(io.github.sspanak.tt9.ime.TraditionalT9 tt9) {
		return tt9 != null && tt9.onKeyFilterClear(false);
	}
}
