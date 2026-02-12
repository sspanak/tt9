package io.github.sspanak.tt9.commands;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdBack implements Command {
	public static final String ID = "cmd_back";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return R.drawable.ic_keyboard; }
	@Override public int getName() { return 0; }

	@Override
	public boolean run(TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}
		tt9.onBack();
		return true;
	}
}
