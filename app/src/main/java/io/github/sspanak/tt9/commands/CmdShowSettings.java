package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdShowSettings implements Command {
	public static final String ID = "key_show_settings";
	public String getId() { return ID; }
	public int getIcon() { return io.github.sspanak.tt9.R.drawable.ic_fn_settings; }
	public int getName() { return io.github.sspanak.tt9.R.string.function_show_settings; }
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 != null) {
			tt9.showSettings();
			return true;
		}
		return false;
	}
}
