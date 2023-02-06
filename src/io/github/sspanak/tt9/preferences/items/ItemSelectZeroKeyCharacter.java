package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.content.res.Resources;

import androidx.preference.DropDownPreference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;

public class ItemSelectZeroKeyCharacter {
	public static final String NAME = "pref_double_zero_char";

	private final DropDownPreference item;
	private final LinkedHashMap<String, String> KEYS = new LinkedHashMap<>();

	public ItemSelectZeroKeyCharacter(DropDownPreference dropDown, Context context) {
		this.item = dropDown;

		Resources resources = context.getResources();

		KEYS.put(".", resources.getString(R.string.char_dot));
		KEYS.put("\\n", resources.getString(R.string.char_newline)); // SharedPreferences return a corrupted string when using the real "\n"... :(
		KEYS.put(" ", resources.getString(R.string.char_space));
	}


	public ItemSelectZeroKeyCharacter populate() {
		if (item == null) {
			Logger.w("tt9/ItemSelectZeroKeyChar.populate", "Cannot populate a NULL item. Ignoring.");
			return this;
		}

		item.setEntries(KEYS.values().toArray(new CharSequence[0]));
		item.setEntryValues(KEYS.keySet().toArray(new CharSequence[0]));
		previewSelection(item.getValue());

		return this;
	}


	public ItemSelectZeroKeyCharacter activate() {
		if (item == null) {
			Logger.w("tt9/ItemSelectZeroKeyChar.activate", "Cannot set a click listener a NULL item. Ignoring.");
			return this;
		}

		item.setOnPreferenceChangeListener((preference, newChar) -> {
			((DropDownPreference) preference).setValue(newChar.toString());
			previewSelection(newChar.toString());
			return true;
		});

		return this;
	}


	private void previewSelection(String newChar) {
		if (item == null) {
			return;
		}

		item.setSummary(KEYS.get(newChar));
	}
}
