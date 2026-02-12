package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import io.github.sspanak.tt9.BuildConfig;

class BaseSettings {
	protected final String LOG_TAG = SettingsInput.class.getSimpleName();

	private static final String FIRST_INSTALL_VERSION_KEY = "first_install_version";

	protected final Context context;
	protected final SharedPreferences prefs;
	private SharedPreferences.Editor prefsEditor;


	BaseSettings(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		setFirstInstallVersion();
	}

	public SharedPreferences.Editor getPrefsEditor() {
		if (prefsEditor == null) {
			prefsEditor = prefs.edit();
		}

		return prefsEditor;
	}

	protected int getStringifiedInt(String key, int defaultValue) {
		try {
			return Integer.parseInt(prefs.getString(key, String.valueOf(defaultValue)));
		} catch (NumberFormatException ignored) {
			return defaultValue;
		}
	}

	protected float getStringifiedFloat(String key, float defaultValue) {
		try {
			return Float.parseFloat(prefs.getString(key, String.valueOf(defaultValue)));
		} catch (NumberFormatException ignored) {
			return defaultValue;
		}
	}

	private void setFirstInstallVersion() {
		if (prefs.getInt(FIRST_INSTALL_VERSION_KEY, -1) == -1) {
			getPrefsEditor().putInt(FIRST_INSTALL_VERSION_KEY, BuildConfig.VERSION_CODE).apply();
		}
	}

	protected boolean isFirstInstall() {
		return prefs.getInt(FIRST_INSTALL_VERSION_KEY, BuildConfig.VERSION_CODE) == BuildConfig.VERSION_CODE;
	}
}
