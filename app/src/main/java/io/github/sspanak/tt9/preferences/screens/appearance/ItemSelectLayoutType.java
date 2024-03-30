package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsUI;

public class ItemSelectLayoutType extends ItemDropDown {
	public static final String NAME = "pref_layout_type";

	private final Context context;

	public ItemSelectLayoutType(Context context, DropDownPreference item) {

		super(item);
		this.context = context;
	}

	public ItemDropDown populate() {
		LinkedHashMap<Integer, String> items = new LinkedHashMap<>();
		items.put(SettingsUI.LAYOUT_STEALTH, context.getString(R.string.pref_layout_stealth));
		items.put(SettingsUI.LAYOUT_TRAY, context.getString(R.string.pref_layout_tray));
		items.put(SettingsUI.LAYOUT_SMALL, context.getString(R.string.pref_layout_small));
		items.put(SettingsUI.LAYOUT_NUMPAD, context.getString(R.string.pref_layout_numpad));

		super.populate(items);

		return this;
	}
}
