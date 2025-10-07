package io.github.sspanak.tt9.commands;

import io.github.sspanak.tt9.R;

public class CmdShift implements Command {
	public static final String ID = "key_shift";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_fn_shift_low; }
	public int getIconCaps() { return R.drawable.ic_fn_shift_caps; }
	public int getIconUp() { return R.drawable.ic_fn_shift_up; }
	public int getName() { return R.string.virtual_key_shift; }

	public static boolean run(io.github.sspanak.tt9.ime.TraditionalT9 tt9) {
		return tt9 != null && tt9.onKeyNextTextCase(false);
	}
}
