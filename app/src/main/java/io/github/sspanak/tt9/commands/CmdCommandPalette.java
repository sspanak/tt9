package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdCommandPalette implements Command {
	public static final String ID = "key_command_palette";
	public String getId() { return ID; }
	public int getIconText() { return R.string.virtual_key_command_palette; }
	public int getIcon() { return 0; }
	public int getName() { return io.github.sspanak.tt9.R.string.function_show_command_palette; }

	public static boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}
		tt9.onKeyCommandPalette(false);
		return true;
	}
}
