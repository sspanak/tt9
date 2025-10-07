package io.github.sspanak.tt9.commands;

public class CmdMoveCursor implements Command {
	public static final String ID = "move_cursor";
	public String getId() { return ID; }
	public int getIcon() { return -1; }
	public int getName() { return 0; }

	public static boolean run(io.github.sspanak.tt9.ime.TraditionalT9 tt9, boolean backward) {
		return tt9 != null && tt9.onKeyMoveCursor(backward);
	}
}
