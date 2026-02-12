package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdRedo implements Command {
	public static final String ID = "key_redo";
	public static final String iconTxt = "↷";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return R.drawable.ic_fn_redo; }
	@Override public int getName() { return R.string.function_redo; }
	@Override public boolean run(@Nullable TraditionalT9 tt9) { return tt9 != null && tt9.onKeyRedo(false); }
	@Override public int getHardKey() { return 6; }
	@Override public int getPaletteKey() { return R.id.soft_key_6; }

	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return tt9 != null && !tt9.isInputLimited();
	}
}
