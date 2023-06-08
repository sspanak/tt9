package io.github.sspanak.tt9.preferences.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import java.util.LinkedHashMap;
import java.util.Set;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class Hotkeys {
	private final Context context;
	private final Resources resources;
	private final String holdKeyTranslation;

	private final LinkedHashMap<String, String> KEYS = new LinkedHashMap<>();


	public Hotkeys(Context context) {
		this.context = context;
		resources = context.getResources();
		holdKeyTranslation = resources.getString(R.string.key_hold_key);

		addNoKey();
		generateList();
	}


	public String get(String key) {
		return KEYS.get(key);
	}


	public Set<String> toSet() {
		return KEYS.keySet();
	}


	/**
	 * setDefault
	 * Applies the default hotkey scheme.
	 *
	 * When a standard "Backspace" hardware key is available, "Backspace" hotkey association is not necessary,
	 * so it will be left out blank, to allow the hardware key do its job.
	 * When the on-screen keyboard is on, "Back" is also not associated, because it will cause weird user
	 * experience. Instead the on-screen "Backspace" key can be used.
	 *
	 * Arrow keys for manipulating suggestions are also assigned only if available.
	 */
	public static void setDefault(SettingsStore settings) {
		int backspaceKeyCode = KeyEvent.KEYCODE_BACK;
		if (
			KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_CLEAR)
			|| KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DEL)
			|| settings.getShowSoftNumpad()
		) {
			backspaceKeyCode = 0;
		}

		settings.setDefaultKeys(
			KeyEvent.KEYCODE_STAR,
			backspaceKeyCode,
			KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_LEFT) ? KeyEvent.KEYCODE_DPAD_LEFT : 0,
			KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_RIGHT) ? KeyEvent.KEYCODE_DPAD_RIGHT : 0,
			KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_DOWN) ? KeyEvent.KEYCODE_DPAD_DOWN : 0,
			KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_UP) ? KeyEvent.KEYCODE_DPAD_UP : 0,
			KeyEvent.KEYCODE_POUND,
			-KeyEvent.KEYCODE_POUND, // negative means "hold"
			-KeyEvent.KEYCODE_STAR
		);
	}


	/**
	 * addIfDeviceHasKey
	 * Add the key only if Android says the device has such keypad button or a permanent touch key.
	 */
	private void addIfDeviceHasKey(int code, String name, boolean allowHold) {
		if (
			(code == KeyEvent.KEYCODE_MENU && ViewConfiguration.get(context).hasPermanentMenuKey())
			|| KeyCharacterMap.deviceHasKey(code)
		) {
			add(code, name, allowHold);
		}
	}


	/**
	 * addIfDeviceHasKey
	 * Same as addIfDeviceHasKey, but accepts a Resource String as a key name.
	 *
	 */
	@SuppressWarnings("SameParameterValue")
	private void addIfDeviceHasKey(int code, int nameResource, boolean allowHold) {
		addIfDeviceHasKey(code, resources.getString(nameResource), allowHold);
	}


	/**
	 * add
	 * These key will be added as a selectable option, regardless if it exists or or not.
	 * No validation will be performed.
	 */
	private void add(int code, String name, boolean allowHold) {
		KEYS.put(String.valueOf(code), name);

		if (allowHold) {
			KEYS.put(String.valueOf(-code), name + " " + holdKeyTranslation);
		}
	}

	/**
	 * add
	 * Same as add(), but accepts a Resource String as a key name.
	 */
	@SuppressWarnings("SameParameterValue")
	private void add(int code, int nameResource, boolean allowHold) {
		add(code, resources.getString(nameResource), allowHold);
	}


	/**
	 * addNoKey
	 * This is the "--" option. The key code matches no key on the keypad.
	 */
	private void addNoKey() {
		add(0, R.string.key_none, false);
	}


	/**
	 * generateList
	 * Generates a list of all supported hotkeys for associating functions in the Settings.
	 *
	 * NOTE: Some TT9 functions do not support all keys. Here you just list all possible options.
	 * Actual validation and assigning happens in SectionKeymap.populate().
	 */
	private void generateList() {
		add(KeyEvent.KEYCODE_CALL, R.string.key_call, true);

		addIfDeviceHasKey(KeyEvent.KEYCODE_BACK, R.string.key_back, false);
		addIfDeviceHasKey(KeyEvent.KEYCODE_F1, "F1", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_F2, "F2", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_F3, "F3", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_F4, "F4", true);

		addIfDeviceHasKey(KeyEvent.KEYCODE_MENU, R.string.key_menu, true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_SOFT_LEFT, R.string.key_soft_left, false);
		addIfDeviceHasKey(KeyEvent.KEYCODE_SOFT_RIGHT, R.string.key_soft_right, false);

		add(KeyEvent.KEYCODE_POUND, "#", true);
		add(KeyEvent.KEYCODE_STAR, "✱", true);

		addIfDeviceHasKey(KeyEvent.KEYCODE_DPAD_UP, R.string.key_dpad_up, false);
		addIfDeviceHasKey(KeyEvent.KEYCODE_DPAD_DOWN, R.string.key_dpad_down, false);
		addIfDeviceHasKey(KeyEvent.KEYCODE_DPAD_LEFT, R.string.key_dpad_left, false);
		addIfDeviceHasKey(KeyEvent.KEYCODE_DPAD_RIGHT, R.string.key_dpad_right, false);

		addIfDeviceHasKey(KeyEvent.KEYCODE_NUMPAD_ADD, "Num +", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_NUMPAD_SUBTRACT, "Num -", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_NUMPAD_MULTIPLY, "Num *", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_NUMPAD_DIVIDE, "Num /", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_NUMPAD_DOT, "Num .", true);

		addIfDeviceHasKey(KeyEvent.KEYCODE_VOLUME_DOWN, R.string.key_volume_down, false);
		addIfDeviceHasKey(KeyEvent.KEYCODE_VOLUME_UP, R.string.key_volume_up, false);

	}
}
