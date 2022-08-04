package io.github.sspanak.tt9.preferences;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import android.view.KeyEvent;

public class T9Preferences {
	private SharedPreferences prefs;

	private int inputMode;
	private int language;

	private int keyBackspace;
	private int keyInputMode;
	private int keyOtherActions;

	private boolean softBackspaceEnabled;
	private boolean softPrefsEnabled;

	public T9Preferences (Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		loadSettings();
	}

	private void loadSettings() {
		inputMode = prefs.getInt("pref_inputmode", 0);
		language = prefs.getInt("pref_language", 1);

		keyBackspace = prefs.getInt("pref_key_backspace", KeyEvent.KEYCODE_DEL);
		keyInputMode = prefs.getInt("pref_key_inputmode", KeyEvent.KEYCODE_POUND);
		keyOtherActions = prefs.getInt("pref_key_other_actions", KeyEvent.KEYCODE_CALL);

		softBackspaceEnabled = prefs.getBoolean("pref_softkey_backspace", true);
		softPrefsEnabled = prefs.getBoolean("pref_softkey_prefs", true);
	}

	public int getInputMode() {
		return inputMode;
	}

	public int getLanguage() {
		return language;
	}

	public int getKeyBackspace() {
		return keyBackspace;
	}

	public int getKeyInputMode() {
		return keyInputMode;
	}

	public int getKeyOtherActions() {
		return keyOtherActions;
	}

	public boolean getSoftBackspaceEnabled() {
		return softBackspaceEnabled;
	}

	public boolean getSoftPrefsEnabled() {
		return softBackspaceEnabled;
	}
}
