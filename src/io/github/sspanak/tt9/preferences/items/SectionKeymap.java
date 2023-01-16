package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.content.res.Resources;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import androidx.preference.DropDownPreference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class SectionKeymap {
	public static final String ITEM_ADD_WORD = "key_add_word";
	public static final String ITEM_BACKSPACE = "key_backspace";
	public static final String ITEM_NEXT_INPUT_MODE = "key_next_input_mode";
	public static final String ITEM_NEXT_LANGUAGE = "key_next_language";
	public static final String ITEM_SHOW_SETTINGS = "key_show_settings";

	private final LinkedHashMap<String, String> KEYS = new LinkedHashMap<>();
	private final Collection<DropDownPreference> items;
	private final SettingsStore settings;


	public SectionKeymap(Collection<DropDownPreference> dropDowns, Context context, SettingsStore settings) {
		items = dropDowns;
		this.settings = settings;
		generateKeyList(context);
	}


	public void reloadSettings() {
		for (DropDownPreference dropDown : items) {
			int keypadKey = settings.getFunctionKey(dropDown.getKey());
			dropDown.setValue(String.valueOf(keypadKey));
			previewCurrentKey(dropDown);
		}
	}


	public SectionKeymap populate() {
		populateOtherItems(null);
		return this;
	}


	public SectionKeymap activate() {
		for (DropDownPreference item : items) {
			onItemClick(item);
		}

		return this;
	}


	/**
	 * generateKeyList
	 * Generates the key list to be used in each dropdown.
	 *
	 * NOTE: Some dropdowns do not support some keys,but filtering is performed
	 * in populate(), not here.
	 *
	 * NOTE 2: Holding is deliberately skipped for most of the keys. It's because
	 * when a function is assigned only to the "hold" state, the "short press" state
	 * also gets consumed in KeyPadHandler, effectively disabling the default function of the key.
	 * However, this may be confusing from user perspective, so in order to avoid lengthy
	 * explanations in the documentation (that no one reads), the problem is avoided by
	 * simply not causing it.
	 *
	 * So, if adding support for new keys, think twice whether to add the "hold" variant as well.
	 */
	private void generateKeyList(Context context) {
		Resources resources = context.getResources();

		KEYS.put(String.valueOf(0), resources.getString(R.string.key_none));

		// BACK
		if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)) {
			KEYS.put(String.valueOf(KeyEvent.KEYCODE_BACK), resources.getString(R.string.key_back));
		}

		// CALL
		KEYS.put(String.valueOf(KeyEvent.KEYCODE_CALL), resources.getString(R.string.key_call));

		// DELETE / BACKSPACE
		if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DEL)) {
			KEYS.put(String.valueOf(KeyEvent.KEYCODE_DEL), resources.getString(R.string.key_delete));
		}

		// F1
		if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_F1)) {
			KEYS.put(String.valueOf(KeyEvent.KEYCODE_F1), resources.getString(R.string.key_f1));
			KEYS.put(
				String.valueOf(-KeyEvent.KEYCODE_F1),
				resources.getString(R.string.key_f1) + " " + resources.getString(R.string.key_hold_key)
			);
		}

		// F2
		if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_F2)) {
			KEYS.put(String.valueOf(KeyEvent.KEYCODE_F2), resources.getString(R.string.key_f2));
			KEYS.put(
				String.valueOf(-KeyEvent.KEYCODE_F2),
				resources.getString(R.string.key_f2) + " " + resources.getString(R.string.key_hold_key)
			);
		}

		// F3
		if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_F3)) {
			KEYS.put(String.valueOf(KeyEvent.KEYCODE_F3), resources.getString(R.string.key_f3));
			KEYS.put(
				String.valueOf(-KeyEvent.KEYCODE_F3),
				resources.getString(R.string.key_f3) + " " + resources.getString(R.string.key_hold_key)
			);
		}

		// F4
		if (KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_F4)) {
			KEYS.put(String.valueOf(KeyEvent.KEYCODE_F4), resources.getString(R.string.key_f4));
			KEYS.put(
				String.valueOf(-KeyEvent.KEYCODE_F4),
				resources.getString(R.string.key_f4) + " " + resources.getString(R.string.key_hold_key)
			);
		}

		// MENU
		if (ViewConfiguration.get(context).hasPermanentMenuKey()) {
			KEYS.put(String.valueOf(KeyEvent.KEYCODE_MENU), resources.getString(R.string.key_menu));
		}

		// #
		KEYS.put(String.valueOf(KeyEvent.KEYCODE_POUND), resources.getString(R.string.key_pound));
		KEYS.put(
			String.valueOf(-KeyEvent.KEYCODE_POUND),
			resources.getString(R.string.key_pound) + " " + resources.getString(R.string.key_hold_key)
		);

		// *
		KEYS.put(String.valueOf(KeyEvent.KEYCODE_STAR), resources.getString(R.string.key_star));
		KEYS.put(
			String.valueOf(-KeyEvent.KEYCODE_STAR),
			resources.getString(R.string.key_star) + " " + resources.getString(R.string.key_hold_key)
		);
	}


	private void populateOtherItems(DropDownPreference itemToSkip) {
		for (DropDownPreference item : items) {
			if (itemToSkip != null && item != null && Objects.equals(itemToSkip.getKey(), item.getKey())) {
				continue;
			}
			populateItem(item);
			previewCurrentKey(item);
		}
	}


	private void populateItem(DropDownPreference dropDown) {
		if (dropDown == null) {
			Logger.w("tt9/SectionKeymap.populateItem", "Cannot populate a NULL item. Ignoring.");
			return;
		}

		ArrayList<String> keys = new ArrayList<>();
		for (String key : KEYS.keySet()) {
			if (
				validateKey(dropDown, String.valueOf(key))
				// backspace works both when pressed short and long,
				// so separate "hold" and "not hold" options for it make no sense
				&& !(dropDown.getKey().equals(ITEM_BACKSPACE) && Integer.parseInt(key) < 0)
				// "show settings" must always be available for the users not to lose
				// access to the Settings screen
				&& !(dropDown.getKey().equals(ITEM_SHOW_SETTINGS) && key.equals("0"))
			) {
				keys.add(String.valueOf(key));
			}
		}

		ArrayList<String> values = new ArrayList<>();
		for (String key : keys) {
			values.add(KEYS.get(key));
		}

		dropDown.setEntries(values.toArray(new CharSequence[0]));
		dropDown.setEntryValues(keys.toArray(new CharSequence[0]));
	}


	private void onItemClick(DropDownPreference item) {
		if (item == null) {
			Logger.w("tt9/SectionKeymap.populateItem", "Cannot set a click listener a NULL item. Ignoring.");
			return;
		}

		item.setOnPreferenceChangeListener((preference, newKey) -> {
			if (!validateKey((DropDownPreference) preference, newKey.toString())) {
				return false;
			}

			((DropDownPreference) preference).setValue(newKey.toString());
			previewCurrentKey((DropDownPreference) preference, newKey.toString());
			populateOtherItems((DropDownPreference) preference);
			return true;
		});
	}


	private void previewCurrentKey(DropDownPreference dropDown) {
		previewCurrentKey(dropDown, dropDown.getValue());
	}


	private void previewCurrentKey(DropDownPreference dropDown, String key) {
		if (dropDown == null) {
			return;
		}

		dropDown.setSummary(KEYS.get(key));
	}


	private boolean validateKey(DropDownPreference dropDown, String key) {
		if (dropDown == null || key == null) {
			return false;
		}

		if (key.equals("0")) {
			return true;
		}

		for (DropDownPreference item : items) {
			if (item != null && !dropDown.getKey().equals(item.getKey()) && key.equals(item.getValue())) {
				Logger.i("tt9/SectionKeymap.validateKey", "Key: '" + key + "' is already in use for function: " + item.getKey());
				return false;
			}
		}

		return true;
	}
}
