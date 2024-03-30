package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

class SettingsUI extends SettingsTyping {
	SettingsUI(Context context) { super(context); }

	public boolean getDarkTheme() {
		int theme = getTheme();
		if (theme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
			return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
		} else {
			return theme == AppCompatDelegate.MODE_NIGHT_YES;
		}
	}

	public int getTheme() {
		try {
			return Integer.parseInt(prefs.getString("pref_theme", String.valueOf(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)));
		} catch (NumberFormatException e) {
			return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
		}
	}

	public boolean getShowSoftKeys() { return prefs.getBoolean("pref_show_soft_keys", true); }

	public boolean getShowSoftNumpad() { return getShowSoftKeys() && prefs.getBoolean("pref_show_soft_numpad", false); }
}
