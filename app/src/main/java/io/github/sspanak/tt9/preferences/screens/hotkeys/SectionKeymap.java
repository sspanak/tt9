package io.github.sspanak.tt9.preferences.screens.hotkeys;

import androidx.preference.DropDownPreference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.helpers.Hotkeys;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class SectionKeymap {
	public static final String ITEM_ADD_WORD = "key_add_word";
	public static final String ITEM_BACKSPACE = "key_backspace";
	public static final String ITEM_COMMAND_PALETTE = "key_command_palette";
	public static final String ITEM_EDIT_TEXT = "key_edit_text";
	public static final String ITEM_FILTER_CLEAR = "key_filter_clear";
	public static final String ITEM_FILTER_SUGGESTIONS = "key_filter_suggestions";
	public static final String ITEM_PREVIOUS_SUGGESTION = "key_previous_suggestion";
	public static final String ITEM_NEXT_SUGGESTION = "key_next_suggestion";
	public static final String ITEM_NEXT_INPUT_MODE = "key_next_input_mode";
	public static final String ITEM_NEXT_LANGUAGE = "key_next_language";
	public static final String ITEM_TAB = "key_tab";
	public static final String ITEM_SELECT_KEYBOARD = "key_select_keyboard";
	public static final String ITEM_SHOW_SETTINGS = "key_show_settings";

	private final Hotkeys hotkeys;
	private final Collection<DropDownPreference> items;
	private final SettingsStore settings;


	public SectionKeymap(Collection<DropDownPreference> dropDowns, PreferencesActivity activity) {
		items = dropDowns;
		hotkeys = new Hotkeys(activity);
		this.settings = activity.getSettings();
	}


	public void reloadSettings() {
		for (DropDownPreference dropDown : items) {
			int keypadKey = settings.getFunctionKey(dropDown.getKey());
			dropDown.setValue(String.valueOf(keypadKey));
		}
		populate();
	}


	public SectionKeymap populate() {
		populateOtherItems(null);
		return this;
	}


	public void activate() {
		for (DropDownPreference item : items) {
			onItemClick(item);
		}
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
			Logger.w("SectionKeymap.populateItem", "Cannot populate a NULL item. Ignoring.");
			return;
		}

		ArrayList<String> keys = new ArrayList<>();
		for (String key : hotkeys.getHardwareKeys()) {
			if (
				validateKey(dropDown, String.valueOf(key))
				// backspace works both when pressed short and long,
				// so separate "hold" and "not hold" options for it make no sense
				&& !(dropDown.getKey().equals(ITEM_BACKSPACE) && Integer.parseInt(key) < 0)
			) {
				keys.add(String.valueOf(key));
			}
		}

		ArrayList<String> values = new ArrayList<>();
		for (String key : keys) {
			values.add(hotkeys.getHardwareKeyName(key));
		}

		dropDown.setEntries(values.toArray(new CharSequence[0]));
		dropDown.setEntryValues(keys.toArray(new CharSequence[0]));
	}


	private void onItemClick(DropDownPreference item) {
		if (item == null) {
			Logger.w("SectionKeymap.populateItem", "Cannot set a click listener a NULL item. Ignoring.");
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
		previewCurrentKey(dropDown, dropDown != null ? dropDown.getValue() : null);
	}


	private void previewCurrentKey(DropDownPreference dropDown, String key) {
		if (dropDown == null) {
			return;
		}

		dropDown.setSummary(hotkeys.getHardwareKeyName(key));
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
				Logger.i("SectionKeymap.validateKey", "Key: '" + key + "' is already in use for function: " + item.getKey());
				return false;
			}
		}

		return true;
	}
}
