package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemNumpadWidth extends ItemDropDown {
	public static final String NAME = "pref_numpad_width";

	private final SettingsStore settings;

	public ItemNumpadWidth(DropDownPreference item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override
	public ItemDropDown populate() {
		LinkedHashMap<Integer, String> options = new LinkedHashMap<>();
		options.put(70, "70 ％");
		options.put(75, "75 ％");
		options.put(80, "80 ％");
		options.put(85, "85 ％");
		options.put(90, "90 ％");
		options.put(95, "95 ％");
		options.put(100, "100 ％");
		super.populateIntegers(options);

		float currentValue = settings.getNumpadWidthPercent();
		currentValue = Math.round(currentValue / 5f) * 5f;
		currentValue = Math.max(Math.min(currentValue, 100f), 50f);

		super.setValue((int) currentValue + "");
		onLayoutChange(settings.getMainViewLayout());

		return this;
	}

	void onLayoutChange(int mainViewLayout) {
		if (item != null) {
			item.setEnabled(mainViewLayout == SettingsStore.LAYOUT_NUMPAD);
		}
	}
}
