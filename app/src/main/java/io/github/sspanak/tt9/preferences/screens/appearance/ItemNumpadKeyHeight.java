package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemNumpadKeyHeight extends ItemDropDown {
	public static final String NAME = "pref_numpad_key_height";

	private final SettingsStore settings;

	public ItemNumpadKeyHeight(DropDownPreference item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override
	public ItemDropDown populate() {
		int baseSize = settings.getNumpadKeyDefaultHeight();

		LinkedHashMap<Integer, String> options = new LinkedHashMap<>();
		options.put((int) Math.round(baseSize * 0.75), "75 ％");
		options.put((int) Math.round(baseSize * 0.875), "87.5 ％");
		options.put(baseSize, "100 ％");
		options.put((int) Math.round(baseSize * 1.15), "115 ％");
		options.put((int) Math.round(baseSize * 1.333), "133 ％");

		super.populateIntegers(options);
		super.setValue(settings.getNumpadKeyHeight() + "");

		return this;
	}
}
