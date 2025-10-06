package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.DropDownPreference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemNumpadShape extends ItemDropDown implements ItemLayoutChangeReactive {
	static final String NAME = "pref_numpad_shape";

	private final SettingsStore settings;

	public ItemNumpadShape(DropDownPreference item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override
	public ItemDropDown populate() {
		if (item == null) {
			return this;
		}

		add(SettingsStore.NUMPAD_SHAPE_SQUARE, R.string.pref_numpad_shape_square);
		add(SettingsStore.NUMPAD_SHAPE_V, R.string.pref_numpad_shape_v);
		add(SettingsStore.NUMPAD_SHAPE_LONG_SPACE, R.string.pref_numpad_shape_long_space);
		commitOptions();
		setValue(String.valueOf(settings.getNumpadShape()));
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
