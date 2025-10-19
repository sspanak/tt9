package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.StatusIcon;

public class CmdNextInputMode implements Command {
	public static final String ID = "key_next_input_mode";
	public String getId() { return ID; }
	public int getIcon() { return StatusIcon.getCachedResourceId(); }
	public int getName() { return R.string.function_next_mode; }

	public boolean run(@Nullable TraditionalT9 tt9) {
		return tt9 != null && tt9.onKeyNextInputMode(false);
	}
}
