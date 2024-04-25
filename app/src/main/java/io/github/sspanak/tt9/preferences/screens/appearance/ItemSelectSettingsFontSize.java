package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemSelectSettingsFontSize extends ItemDropDown {
	public static final String NAME = "pref_font_size";
	private final AppearanceScreen screen;

	public ItemSelectSettingsFontSize(DropDownPreference item, AppearanceScreen screen) {
		super(item);
		this.screen = screen;
	}

	public ItemDropDown populate() {
		LinkedHashMap<Integer, String> themes = new LinkedHashMap<>();
		themes.put(SettingsStore.FONT_SIZE_DEFAULT, screen.getString(R.string.pref_font_size_default));
		themes.put(SettingsStore.FONT_SIZE_LARGE, screen.getString(R.string.pref_font_size_large));

		super.populateIntegers(themes);
		setValue(String.valueOf(new SettingsStore(screen.getContext()).getSettingsFontSize()));

		return this;
	}

	@Override
	protected boolean onClick(Preference preference, Object newSize) {
		if (super.onClick(preference, newSize)) {
			screen.resetFontSize(true);
			return true;
		}

		return false;
	}
}
