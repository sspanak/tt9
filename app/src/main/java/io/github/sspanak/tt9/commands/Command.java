package io.github.sspanak.tt9.commands;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;

public interface Command {
	String getId();
	int getIcon();
	int getName();
	default String getName(@NonNull Context context) { return context.getString(getName()); }
	default boolean run(@Nullable TraditionalT9 tt9) { return false; }
}
