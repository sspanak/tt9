package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemWidth extends ItemDropDown implements ItemLayoutChangeReactive {
	public static final String NAME = "pref_numpad_width";

	private final SettingsStore settings;

	public ItemWidth(DropDownPreference item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override
	public ItemDropDown populate() {
		LinkedHashMap<Integer, String> options = new LinkedHashMap<>();
		for (int i = SettingsStore.MIN_WIDTH_PERCENT; i <= 100; i += 5) {
			options.put(i, i + " ％");
		}
		super.populateIntegers(options);

		float currentValue = settings.getWidthPercent();
		currentValue = Math.round(currentValue / 5f) * 5f;
		currentValue = Math.max(Math.min(currentValue, 100f), 50f);

		super.setValue(String.valueOf((int) currentValue));
		onLayoutChange(settings.getMainViewLayout());

		return this;
	}

	public void onLayoutChange(int mainViewLayout) {
		if (item != null) {
			item.setEnabled(mainViewLayout != SettingsStore.LAYOUT_STEALTH);
		}
	}
}
