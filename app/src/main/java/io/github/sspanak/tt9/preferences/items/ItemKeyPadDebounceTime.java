package io.github.sspanak.tt9.preferences.items;

import android.content.Context;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;

public class ItemKeyPadDebounceTime extends ItemDropDown {
	public static final String NAME = "pref_key_pad_debounce_time";

	private final Context context;

	public ItemKeyPadDebounceTime(Context context, DropDownPreference item) {
		super(item);
		this.context = context;
	}

	public ItemDropDown populate() {
		LinkedHashMap<Integer, String> dropDownOptions = new LinkedHashMap<>();
		dropDownOptions.put(0, context.getString(R.string.pref_hack_key_pad_debounce_off));

		int[] values = new int[] { 33, 50, 100, 150, 250, 350 };
		for (int value : values) {
			dropDownOptions.put(value, value + " ms");
		}
		super.populate(dropDownOptions);

		return this;
	}
}
