package io.github.sspanak.tt9.util;

import android.content.Context;
import android.content.res.Configuration;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ThemedContextBuilder {
	private Configuration configuration;
	private Context context;
	private int nightModeFlag;
	private int theme;


	public ThemedContextBuilder setConfiguration(@NonNull Configuration configuration) {
		this.configuration = new Configuration(configuration);
		return this;
	}


	public ThemedContextBuilder setContext(@NonNull Context context) {
		this.context = context;
		return this;
	}


	public ThemedContextBuilder setSettings(@NonNull SettingsStore settings) {
		nightModeFlag = settings.getDarkTheme() ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
		return this;
	}

	public ThemedContextBuilder setTheme(int theme) {
		this.theme = theme;
		return this;
	}

	/**
	 * getThemedContext
	 * 1. Overrides the system dark/light them with the one in our settings.
	 * 2. Fixes this error log: "View class SoftKeyXXX is an AppCompat widget that can only be used
	 * with a Theme.AppCompat theme (or descendant)."
	 */
	public ContextThemeWrapper build() {
		configuration.uiMode = nightModeFlag | (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK);
		ContextThemeWrapper themedCtx = new ContextThemeWrapper(context, theme);
		themedCtx.applyOverrideConfiguration(configuration);

		return themedCtx;
	}
}
