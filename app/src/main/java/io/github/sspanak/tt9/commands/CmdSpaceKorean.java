package io.github.sspanak.tt9.commands;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdSpaceKorean implements Command {
	public static final String ID = "key_space_korean";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_fn_space; }
	public int getName() { return R.string.virtual_key_space_korean; }
	public static boolean run(TraditionalT9 tt9) { return tt9 != null && tt9.onKeySpaceKorean(false); }
}
