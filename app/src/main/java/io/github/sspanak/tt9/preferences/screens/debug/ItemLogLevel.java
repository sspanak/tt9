package io.github.sspanak.tt9.preferences.screens.debug;

import android.util.Log;

import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.util.Logger;

class ItemLogLevel extends ItemDropDown {
	public static final String NAME = "pref_log_level";

	ItemLogLevel(DropDownPreference item) {
		super(item);
	}

	public ItemLogLevel populate() {
		LinkedHashMap<String, String> values = new LinkedHashMap<>();
		values.put(String.valueOf(Log.VERBOSE), "Verbose");
		values.put(String.valueOf(Log.DEBUG), "Debug");
		values.put(String.valueOf(Log.INFO), "Info");
		values.put(String.valueOf(Log.WARN), "Warning");
		values.put(String.valueOf(Log.ERROR), "Error (default)");

		super.populate(values);
		super.setValue(String.valueOf(Logger.LEVEL));

		return this;
	}

	@Override
	protected boolean onClick(Preference preference, Object newKey) {
		if (super.onClick(preference, newKey)) {
			Logger.setLevel(Integer.parseInt(newKey.toString()));
			return true;
		}

		return false;
	}
}
