package io.github.sspanak.tt9.preferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

public class ItemToggleDarkTheme {
	public static final String NAME = "pref_dark_theme";

	private final SwitchPreferenceCompat themeToggle;

	public ItemToggleDarkTheme(SwitchPreferenceCompat item) {
		themeToggle = item;
	}

	public void enableToggleHandler() {
		themeToggle.setOnPreferenceChangeListener(this::onChange);
	}

	private boolean onChange(Preference p, Object newValue) {
		AppCompatDelegate.setDefaultNightMode(
			((boolean) newValue) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
		);

		return true;
	}
}
