package io.github.sspanak.tt9.commands;

public class CmdNextKeyboard implements Command {
	public static final String ID = "key_next_keyboard";
	public String getId() { return ID; }
	public int getIcon() { return -1; }
	public int getName() { return 0; }

	public static void run(io.github.sspanak.tt9.ime.TraditionalT9 tt9) {
		if (tt9 != null) {
			tt9.nextKeyboard();
		}
	}
}
