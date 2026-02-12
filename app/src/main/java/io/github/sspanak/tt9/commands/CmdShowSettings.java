package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdShowSettings implements Command {
	public static final String ID = "key_show_settings";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return R.drawable.ic_fn_settings; }
	@Override public int getName() { return R.string.function_show_settings; }
	@Override public int getHardKey() { return 9; }
	@Override public int getPaletteKey() { return R.id.soft_key_9; }


	@Override public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 != null) {
			tt9.showSettings();
			return true;
		}
		return false;
	}
}
