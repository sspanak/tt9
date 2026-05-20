package io.github.sspanak.tt9.commands;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;

public class NullCommand implements Command {
	@NonNull public static final NullCommand self = new NullCommand();

	public static final String ID = "null";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return -1; }
	@Override public int getName() { return 0; }
	@Override @Nullable public String getName(@Nullable Context c) { return ""; }
	@Override public boolean runFromHotkey(@Nullable TraditionalT9 tt9, boolean validateOnly) { return false; }
}
