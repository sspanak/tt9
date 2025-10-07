package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdRedo implements Command {
	public static final String ID = "key_redo";
	public static final String iconTxt = "↷";
	public String getId() { return ID; }
	public int getIcon() { return io.github.sspanak.tt9.R.drawable.ic_fn_redo; }
	public int getName() { return io.github.sspanak.tt9.R.string.function_redo; }
	public static boolean run(@Nullable TraditionalT9 tt9) { return tt9 != null && tt9.onKeyRedo(false); }
}
