package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.view.Gravity;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemAlignment extends ItemDropDown implements ItemLayoutChangeReactive {
	public static final String NAME = "pref_numpad_alignment";

	private final SettingsStore settings;

	ItemAlignment(DropDownPreference item, SettingsStore settings) {
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
		options.put(Gravity.START, context.getString(R.string.virtual_numpad_alignment_left));
		options.put(Gravity.CENTER_HORIZONTAL, context.getString(R.string.virtual_numpad_alignment_center));
		options.put(Gravity.END, context.getString(R.string.virtual_numpad_alignment_right));

		super.populateIntegers(options);
		super.setValue(String.valueOf(settings.getAlignment()));
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
