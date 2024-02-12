package io.github.sspanak.tt9.preferences.items;

import android.util.Log;

import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.Logger;

public class ItemLogLevel extends ItemDropDown {
	public static final String NAME = "pref_log_level";

	public ItemLogLevel(DropDownPreference item) {
		super(item);
	}

	public ItemLogLevel populate() {
		LinkedHashMap<Integer, String> values = new LinkedHashMap<>();
		values.put(Log.VERBOSE, "Verbose");
		values.put(Log.DEBUG, "Debug");
		values.put(Log.INFO, "Info");
		values.put(Log.WARN, "Warning");
		values.put(Log.ERROR, "Error (default)");

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
