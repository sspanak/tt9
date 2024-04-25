package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsUI;

public class ItemSelectLayoutType extends ItemDropDown {
	public static final String NAME = "pref_layout_type";

	private final PreferencesActivity activity;

	public ItemSelectLayoutType(DropDownPreference item, PreferencesActivity activity) {
		super(item);
		this.activity = activity;
	}

	public ItemDropDown populate() {
		LinkedHashMap<Integer, String> items = new LinkedHashMap<>();
		items.put(SettingsUI.LAYOUT_STEALTH, activity.getString(R.string.pref_layout_stealth));
		items.put(SettingsUI.LAYOUT_TRAY, activity.getString(R.string.pref_layout_tray));
		items.put(SettingsUI.LAYOUT_SMALL, activity.getString(R.string.pref_layout_small));
		items.put(SettingsUI.LAYOUT_NUMPAD, activity.getString(R.string.pref_layout_numpad));

		super.populateIntegers(items);
		super.setValue(String.valueOf(activity.getSettings().getMainViewLayout()));

		return this;
	}
}
