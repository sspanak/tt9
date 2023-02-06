package io.github.sspanak.tt9.ime.helpers;

import android.view.KeyEvent;

import io.github.sspanak.tt9.preferences.SettingsStore;

public class Key {
	public static boolean isNumber(int keyCode) {
		return keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9;
	}


	public static boolean isHotkey(SettingsStore settings, int keyCode) {
		return keyCode == settings.getKeyAddWord()
			|| keyCode == settings.getKeyBackspace()
			|| keyCode == settings.getKeyNextLanguage()
			|| keyCode == settings.getKeyNextInputMode()
			|| keyCode == settings.getKeyShowSettings();
	}


	public static boolean isOK(int keyCode) {
		return
			keyCode == KeyEvent.KEYCODE_DPAD_CENTER
			|| keyCode == KeyEvent.KEYCODE_ENTER
			|| keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER;
	}


	public static int codeToNumber(int keyCode) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_0:
				return 0;
			case KeyEvent.KEYCODE_1:
				return 1;
			case KeyEvent.KEYCODE_2:
				return 2;
			case KeyEvent.KEYCODE_3:
				return 3;
			case KeyEvent.KEYCODE_4:
				return 4;
			case KeyEvent.KEYCODE_5:
				return 5;
			case KeyEvent.KEYCODE_6:
				return 6;
			case KeyEvent.KEYCODE_7:
				return 7;
			case KeyEvent.KEYCODE_8:
				return 8;
			case KeyEvent.KEYCODE_9:
				return 9;
			default:
				return -1;
		}
	}
}
