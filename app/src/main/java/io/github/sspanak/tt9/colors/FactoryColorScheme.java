package io.github.sspanak.tt9.colors;

import android.content.Context;

import androidx.annotation.NonNull;

public interface FactoryColorScheme {
	AbstractColorScheme create(@NonNull Context context);
}
