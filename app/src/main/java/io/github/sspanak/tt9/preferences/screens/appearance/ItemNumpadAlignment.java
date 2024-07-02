package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemNumpadAlignment extends ItemDropDown {
	public static final String NAME = "pref_numpad_alignment";

	private final SettingsStore settings;

	ItemNumpadAlignment(DropDownPreference item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override
	public ItemDropDown populate() {
		if (item == null) {
			return this;
		}

		Context context = item.getContext();

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			item.setVisible(false);
			return this;
		}

		LinkedHashMap<Integer, String> options = new LinkedHashMap<>();
		options.put(Gravity.START, context.getString(R.string.virtual_numpad_alignment_left));
		options.put(Gravity.CENTER_HORIZONTAL, context.getString(R.string.virtual_numpad_alignment_center));
		options.put(Gravity.END, context.getString(R.string.virtual_numpad_alignment_right));

		super.populateIntegers(options);
		super.setValue(settings.getNumpadAlignment() + "");
		onLayoutChange(settings.getMainViewLayout());

		return this;
	}

	void onLayoutChange(int mainViewLayout) {
		if (item != null) {
			item.setEnabled(mainViewLayout == SettingsStore.LAYOUT_NUMPAD);
		}
	}
}
