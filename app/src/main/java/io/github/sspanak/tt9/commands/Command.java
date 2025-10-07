package io.github.sspanak.tt9.commands;

import android.content.Context;

import androidx.annotation.NonNull;

public interface Command {
	String getId();
	int getIcon();
	int getName();
	default String getName(@NonNull Context context) { return context.getString(getName()); }
}
