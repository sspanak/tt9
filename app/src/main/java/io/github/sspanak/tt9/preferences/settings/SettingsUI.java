package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsUI extends SettingsTyping {
	public final static int LAYOUT_STEALTH = 0;
	public final static int LAYOUT_TRAY = 1;
	public final static int LAYOUT_SMALL = 2;
	public final static int LAYOUT_NUMPAD = 3;

	SettingsUI(Context context) { super(context); }

	public boolean isStatusIconEnabled() { return prefs.getBoolean("pref_status_icon", false); }

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

	public int getMainViewLayout() {
		try {
			return Integer.parseInt(prefs.getString("pref_layout_type", String.valueOf(LAYOUT_SMALL)));
		} catch(NumberFormatException e) {
			return LAYOUT_SMALL;
		}
	}

	public boolean isMainLayoutNumpad() { return getMainViewLayout() == LAYOUT_NUMPAD; }
	public boolean isMainLayoutTray() { return getMainViewLayout() == LAYOUT_TRAY; }
	public boolean isMainLayoutSmall() { return getMainViewLayout() == LAYOUT_SMALL; }
	public boolean isMainLayoutStealth() { return getMainViewLayout() == LAYOUT_STEALTH; }
}
