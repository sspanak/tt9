package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.DropDownPreference;

import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemNumpadKeyFontSize extends ItemDropDown implements ItemLayoutChangeReactive{
	public static final String NAME = "pref_numpad_key_font_size";
	private final SettingsStore settings;

	public ItemNumpadKeyFontSize(DropDownPreference item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override
	public ItemDropDown populate() {
		populatePercentRange(80, 130, 5);
		setValue(String.valueOf(settings.getNumpadKeyFontSizePercent()));
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
