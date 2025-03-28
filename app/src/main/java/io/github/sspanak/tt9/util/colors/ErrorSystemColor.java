package io.github.sspanak.tt9.util.colors;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ErrorSystemColor extends SystemColor {
	public ErrorSystemColor(@NonNull Context context) {
		final boolean isDark = new SettingsStore(context).getDarkTheme();
		final int colorResource = isDark ? android.R.color.holo_red_dark : android.R.color.holo_red_light;
		color = context.getResources().getColor(colorResource);
	}
}
