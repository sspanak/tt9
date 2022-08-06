package io.github.sspanak.tt9.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import android.view.KeyEvent;

public class T9Preferences {
	private static T9Preferences self;

	private SharedPreferences prefs;
	private SharedPreferences.Editor prefsEditor;

	public static final int CASE_LOWER = 0;
	public static final int CASE_CAPITALIZE = 1;
	public static final int CASE_UPPER = 2;

	public static final int MODE_PREDICTIVE = 0;
	public static final int MODE_ABC = 1;
	public static final int MODE_123 = 2;

	public T9Preferences (Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		prefsEditor = prefs.edit();
	}

	public static T9Preferences getInstance(Context context) {
		if (self == null) {
			self = new T9Preferences(context);
		}

		return self;
	}

	public int getEnabledLanguages() {
		return prefs.getInt("pref_enabled_languages", 1);
	}

	public T9Preferences setEnabledLanguages(int languageMask) {
		prefsEditor.putInt("pref_enabled_languages", languageMask);
		prefsEditor.apply();

		return this;
	}

	// public int getInputCase() {
	// 	return prefs.getInt("pref_input_case", CASE_CAPITALIZE);
	// }

	public int getInputLanguage() {
		return prefs.getInt("pref_input_language", 1);
	}

	public T9Preferences setInputLanguage(int language) {
		prefsEditor.putInt("pref_input_language", language);
		prefsEditor.apply();

		return this;
	}

	public int getInputMode() {
		return prefs.getInt("pref_input_mode", MODE_PREDICTIVE);
	}

	public T9Preferences setInputMode(int mode) throws Exception {
		if (mode != MODE_PREDICTIVE && mode != MODE_ABC && mode != MODE_123) {
			throw new Exception("Invalid input mode: '" + mode + "'");
		}

		prefsEditor.putInt("pref_input_mode", mode);
		prefsEditor.apply();

		return this;
	}

	public int getKeyBackspace() {
		return prefs.getInt("pref_key_backspace", KeyEvent.KEYCODE_BACK);
	}

	// public int getKeyInputMode() {
	// 	return prefs.getInt("pref_key_inputmode", KeyEvent.KEYCODE_POUND);
	// }

	// public int getKeyOtherActions() {
	// 	return prefs.getInt("pref_key_other_actions", KeyEvent.KEYCODE_CALL);
	// }

	// public boolean getSoftBackspaceEnabled() {
	// 	return prefs.getBoolean("pref_softkey_backspace", true);
	// }

	// public boolean getSoftPrefsEnabled() {
	// 	return prefs.getBoolean("pref_softkey_prefs", true);
	// }

	public String getLastWord() {
		return prefs.getString("last_word", "");
	}

	public T9Preferences setLastWord(String lastWord) {
		// "last_word" was part of the original Preferences implementation.
		// It is weird, but it is simple and it works, so I decided to keep it.
		prefsEditor.putString("last_word", lastWord);
		prefsEditor.apply();

		return this;
	}
}
