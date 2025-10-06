package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.DropDownPreference;

import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemNumpadKeyHeight extends ItemDropDown implements ItemLayoutChangeReactive {
	public static final String NAME = "pref_numpad_key_height";

	private final SettingsStore settings;

	public ItemNumpadKeyHeight(DropDownPreference item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override
	public ItemDropDown populate() {
		int baseSize = settings.getNumpadKeyDefaultHeight();

		for (int i = 70; i <= 150; i += 5) {
			add(String.valueOf(Math.round(baseSize * i / 100.0)), i + " ％");
		}
		commitOptions();
		setValue(String.valueOf(settings.getNumpadKeyHeight()));
		onLayoutChange(settings.getMainViewLayout());

		return this;
	}

	public void onLayoutChange(int mainViewLayout) {
		if (item != null) {
			item.setVisible(mainViewLayout == SettingsStore.LAYOUT_NUMPAD);
			item.setIconSpaceReserved(false);
		}
	}
}
