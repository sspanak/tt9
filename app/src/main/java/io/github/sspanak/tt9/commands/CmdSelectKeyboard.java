package io.github.sspanak.tt9.commands;

public class CmdSelectKeyboard implements Command {
	public static final String ID = "key_select_keyboard";
	public String getId() { return ID; }
	public int getIcon() { return io.github.sspanak.tt9.R.drawable.ic_fn_next_keyboard; }
	public int getName() { return io.github.sspanak.tt9.R.string.function_select_keyboard; }

	public boolean run(io.github.sspanak.tt9.ime.TraditionalT9 tt9) {
		if (tt9 != null) {
			tt9.selectKeyboard();
			return true;
		}

		return false;
	}
}
