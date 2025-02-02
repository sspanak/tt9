package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemNumpadShape extends ItemDropDown {
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

		Context context = item.getContext();

		LinkedHashMap<Integer, String> options = new LinkedHashMap<>();
		options.put(SettingsStore.NUMPAD_SHAPE_SQUARE, context.getString(R.string.pref_numpad_shape_square));
		options.put(SettingsStore.NUMPAD_SHAPE_V, context.getString(R.string.pref_numpad_shape_v));
		options.put(SettingsStore.NUMPAD_SHAPE_LONG_SPACE, context.getString(R.string.pref_numpad_shape_long_space));

		super.populateIntegers(options);
		super.setValue(String.valueOf(settings.getNumpadShape()));
		onLayoutChange(settings.getMainViewLayout());

		return this;
	}

	void onLayoutChange(int mainViewLayout) {
		if (item != null) {
			item.setVisible(mainViewLayout == SettingsStore.LAYOUT_NUMPAD);
			item.setIconSpaceReserved(false);
		}
	}
}
