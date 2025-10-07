package io.github.sspanak.tt9.commands;

public class NullCommand implements Command {
	public static final String ID = "null";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return -1; }
	@Override public int getName() { return 0; }
}
