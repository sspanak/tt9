package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class ItemSelectSettingsFontSize extends ItemDropDown {
	public static final String NAME = "pref_font_size";
	private final AppearanceScreen screen;

	public ItemSelectSettingsFontSize(DropDownPreference item, AppearanceScreen screen) {
		super(item);
		this.screen = screen;
	}

	public ItemDropDown populate() {
		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			if (item != null) item.setVisible(false);
			return this;
		}

		add(SettingsStore.FONT_SIZE_DEFAULT, R.string.pref_font_size_default);
		add(SettingsStore.FONT_SIZE_LARGE, R.string.pref_font_size_large);
		commitOptions();
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
