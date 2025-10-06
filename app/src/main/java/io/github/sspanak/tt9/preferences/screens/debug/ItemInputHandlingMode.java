package io.github.sspanak.tt9.preferences.screens.debug;

import androidx.preference.DropDownPreference;

import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemInputHandlingMode extends ItemDropDown {
	public static final int NORMAL = 0;
	public static final int RETURN_FALSE = 1;
	public static final int CALL_SUPER = 2;

	public static final String NAME = "pref_input_handling_mode";

	private final SettingsStore settings;

	ItemInputHandlingMode(DropDownPreference item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	public ItemInputHandlingMode populate() {
		add(NORMAL, "Normal");
		add(RETURN_FALSE, "Return False");
		add(CALL_SUPER, "Call Super");
		commitOptions();
		setValue(String.valueOf(settings.getInputHandlingMode()));

		return this;
	}
}
