package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.DropDownPreference;

import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemSuggestionFontSize extends ItemDropDown implements ItemLayoutChangeReactive{
	public static final String NAME = "pref_suggestion_font_size";
	private final SettingsStore settings;

	public ItemSuggestionFontSize(DropDownPreference item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override
	public ItemDropDown populate() {
		populatePercentRange(70, 150, 5);
		setValue(String.valueOf(settings.getSuggestionFontSizePercent()));
		onLayoutChange(settings.getMainViewLayout());

		return this;
	}


	public void onLayoutChange(int mainViewLayout) {
		if (item != null) {
			item.setVisible(mainViewLayout != SettingsStore.LAYOUT_STEALTH);
			item.setIconSpaceReserved(false);
		}
	}
}
