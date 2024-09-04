package io.github.sspanak.tt9.preferences.screens.modeAbc;

import android.content.Context;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;


public class ItemSelectAbcAutoAccceptTime extends ItemDropDown {
	public static final String NAME = "pref_abc_auto_accept_time";
	private final Context context;

	public ItemSelectAbcAutoAccceptTime(DropDownPreference item, Context context) {
		super(item);
		this.context = context;
	}

	public ItemSelectAbcAutoAccceptTime populate() {
		LinkedHashMap<String, String> dropDownOptions = new LinkedHashMap<>();
		dropDownOptions.put("-1", context.getString(R.string.pref_abc_auto_accept_off));
		dropDownOptions.put("350", context.getString(R.string.pref_abc_auto_accept_fastest));
		dropDownOptions.put("500", context.getString(R.string.pref_abc_auto_accept_fast));
		dropDownOptions.put("800", context.getString(R.string.pref_abc_auto_accept_normal));
		dropDownOptions.put("1200", context.getString(R.string.pref_abc_auto_accept_slow));

		super.populate(dropDownOptions);

		return this;
	}
}
