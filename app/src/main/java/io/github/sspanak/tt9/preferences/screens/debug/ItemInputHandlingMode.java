package io.github.sspanak.tt9.preferences.screens.debug;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;

public class ItemInputHandlingMode extends ItemDropDown {
	public static final int NORMAL = 0;
	public static final int RETURN_FALSE = 1;
	public static final int CALL_SUPER = 2;

	public static final String NAME = "pref_input_handling_mode";

	private SettingsStore settings;

	ItemInputHandlingMode(DropDownPreference item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	public ItemInputHandlingMode populate() {
		LinkedHashMap<Integer, String> values = new LinkedHashMap<>();
		values.put(NORMAL, "Normal");
		values.put(RETURN_FALSE, "Return False");
		values.put(CALL_SUPER, "Call Super");

		super.populate(values);
		super.setValue(String.valueOf(settings.getInputHandlingMode()));

		return this;
	}
}
