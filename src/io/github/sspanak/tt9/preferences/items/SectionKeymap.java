package io.github.sspanak.tt9.preferences.items;

import android.content.Context;

import androidx.preference.DropDownPreference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.preferences.helpers.Hotkeys;

public class SectionKeymap {
	public static final String ITEM_ADD_WORD = "key_add_word";
	public static final String ITEM_BACKSPACE = "key_backspace";
	public static final String ITEM_NEXT_INPUT_MODE = "key_next_input_mode";
	public static final String ITEM_NEXT_LANGUAGE = "key_next_language";
	public static final String ITEM_SHOW_SETTINGS = "key_show_settings";

	private final Hotkeys hotkeys;
	private final Collection<DropDownPreference> items;
	private final SettingsStore settings;


	public SectionKeymap(Collection<DropDownPreference> dropDowns, Context context, SettingsStore settings) {
		items = dropDowns;
		hotkeys = new Hotkeys(context);
		this.settings = settings;
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
		for (String key : hotkeys.toSet()) {
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
			values.add(hotkeys.get(key));
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

		dropDown.setSummary(hotkeys.get(key));
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
