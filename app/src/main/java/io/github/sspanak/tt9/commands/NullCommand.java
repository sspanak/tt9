package io.github.sspanak.tt9.commands;

import android.content.Context;

import androidx.annotation.NonNull;

public class NullCommand implements Command {
	public static final String ID = "null";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return -1; }
	@Override public int getName() { return 0; }
	@Override public String getName(@NonNull Context c) { return ""; }
}
