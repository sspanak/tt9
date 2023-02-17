package io.github.sspanak.tt9.ime.helpers;

import android.view.KeyEvent;

import io.github.sspanak.tt9.preferences.SettingsStore;

public class Key {
	public static boolean isNumber(int keyCode) {
		return
			(keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)
			|| (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9);
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


	public static int codeToNumber(SettingsStore settings, int keyCode) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_0:
			case KeyEvent.KEYCODE_NUMPAD_0:
				return 0;
			case KeyEvent.KEYCODE_1:
			case KeyEvent.KEYCODE_NUMPAD_1:
				return settings.getUpsideDownKeys() ? 7 : 1;
			case KeyEvent.KEYCODE_2:
			case KeyEvent.KEYCODE_NUMPAD_2:
				return settings.getUpsideDownKeys() ? 8 : 2;
			case KeyEvent.KEYCODE_3:
			case KeyEvent.KEYCODE_NUMPAD_3:
				return settings.getUpsideDownKeys() ? 9 : 3;
			case KeyEvent.KEYCODE_4:
			case KeyEvent.KEYCODE_NUMPAD_4:
				return 4;
			case KeyEvent.KEYCODE_5:
			case KeyEvent.KEYCODE_NUMPAD_5:
				return 5;
			case KeyEvent.KEYCODE_6:
			case KeyEvent.KEYCODE_NUMPAD_6:
				return 6;
			case KeyEvent.KEYCODE_7:
			case KeyEvent.KEYCODE_NUMPAD_7:
				return settings.getUpsideDownKeys() ? 1 : 7;
			case KeyEvent.KEYCODE_8:
			case KeyEvent.KEYCODE_NUMPAD_8:
				return settings.getUpsideDownKeys() ? 2 : 8;
			case KeyEvent.KEYCODE_9:
			case KeyEvent.KEYCODE_NUMPAD_9:
				return settings.getUpsideDownKeys() ? 3 : 9;
			default:
				return -1;
		}
	}

	public static int numberToCode(int number) {
		if (number >= 0 && number <= 9) {
			return KeyEvent.KEYCODE_0 + number;
		} else {
			return -1;
		}
	}
}
