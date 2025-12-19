package io.github.sspanak.tt9.commands;

public class CmdMoveCursor implements Command {
	public static final int CURSOR_MOVE_UP = 0;
	public static final int CURSOR_MOVE_DOWN = 1;
	public static final int CURSOR_MOVE_LEFT = 2;
	public static final int CURSOR_MOVE_RIGHT = 3;

	public static final String ID = "move_cursor";
	public String getId() { return ID; }
	public int getIcon() { return -1; }
	public int getName() { return 0; }

	public boolean run(io.github.sspanak.tt9.ime.TraditionalT9 tt9, int direction) {
		return tt9 != null && tt9.onKeyMoveCursor(direction);
	}
}
