package io.github.sspanak.tt9.commands;

import io.github.sspanak.tt9.R;

public class CmdSuggestionPrevious implements Command {
	public static final String ID = "key_previous_suggestion";
	public String getId() { return ID; }
	public int getIcon() { return -1; }
	public int getName() { return R.string.function_previous_suggestion; }

	public static boolean run(io.github.sspanak.tt9.ime.TraditionalT9 tt9) {
		return tt9 != null && tt9.onKeyScrollSuggestion(false, true);
	}
}
