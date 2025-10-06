package io.github.sspanak.tt9.preferences.screens.debug;

import android.util.Log;

import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.util.Logger;

class ItemLogLevel extends ItemDropDown {
	public static final String NAME = "pref_log_level";

	ItemLogLevel(DropDownPreference item) {
		super(item);
	}

	public ItemLogLevel populate() {
		add(String.valueOf(Log.VERBOSE), "Verbose");
		add(String.valueOf(Log.DEBUG), "Debug");
		add(String.valueOf(Log.INFO), "Info");
		add(String.valueOf(Log.WARN), "Warning");
		add(String.valueOf(Log.ERROR), "Error (default)");
		commitOptions();
		setValue(String.valueOf(Logger.LEVEL));

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
