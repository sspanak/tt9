package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;

class ItemSelectTheme extends ItemDropDown {
	public static final String NAME = "pref_theme";

	private final Context context;

	ItemSelectTheme(Context context, DropDownPreference item) {
		super(item);
		this.context = context;
	}

	public ItemDropDown populate() {
		LinkedHashMap<Integer, String> themes = new LinkedHashMap<>();
		themes.put(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, context.getString(R.string.pref_dark_theme_auto));
		themes.put(AppCompatDelegate.MODE_NIGHT_NO, context.getString(R.string.pref_dark_theme_no));
		themes.put(AppCompatDelegate.MODE_NIGHT_YES, context.getString(R.string.pref_dark_theme_yes));

		super.populateIntegers(themes);

		return this;
	}

	@Override
	protected boolean onClick(Preference preference, Object newKey) {
		if (super.onClick(preference, newKey)) {
			AppCompatDelegate.setDefaultNightMode(Integer.parseInt(newKey.toString()));
			return true;
		}

		return false;
	}
}
