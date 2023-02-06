package io.github.sspanak.tt9.preferences.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import java.util.LinkedHashMap;
import java.util.Set;

import io.github.sspanak.tt9.R;

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
	 * These keys will appears as options only if Android says the device has them.
	 *
	 * NOTE: Some TT9 functions do not support all keys. Here you just list all possible options.
	 * Actual validation and assigning happens in SectionKeymap.populate().
	 *
	 * NOTE 2: Holding is deliberately skipped for most of the keys.
	 * It's because handling holding requires short press event to be consumed in
	 * KeyPadHandler, as well.
	 *
	 * From user perspective, when holding is assigned to a function,
	 * short press will also stop performing its default system action, which may be confusing.
	 * And in order to avoid lengthy explanations in the documentation (that no one reads),
	 * the problem is avoided by simply not causing it.
	 */
	private void generateList() {
		add(KeyEvent.KEYCODE_CALL, R.string.key_call, false);

		addIfDeviceHasKey(KeyEvent.KEYCODE_BACK, R.string.key_back, false);
		addIfDeviceHasKey(KeyEvent.KEYCODE_DEL, R.string.key_delete, false);
		addIfDeviceHasKey(KeyEvent.KEYCODE_F1, "F1", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_F2, "F2", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_F3, "F3", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_F4, "F4", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_MENU, R.string.key_menu, false);

		add(KeyEvent.KEYCODE_POUND, "#", true);
		add(KeyEvent.KEYCODE_STAR, "✱", true);

		addIfDeviceHasKey(KeyEvent.KEYCODE_NUMPAD_ADD, "Num +", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_NUMPAD_SUBTRACT, "Num -", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_NUMPAD_MULTIPLY, "Num *", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_NUMPAD_DIVIDE, "Num /", true);
		addIfDeviceHasKey(KeyEvent.KEYCODE_NUMPAD_DOT, "Num .", true);
	}
}
