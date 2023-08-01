package io.github.sspanak.tt9.preferences.items;

import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import io.github.sspanak.tt9.Logger;

public class ItemDropDown {
	private final DropDownPreference item;
	private LinkedHashMap<Integer, String> values;

	public ItemDropDown(DropDownPreference item) {
		this.item = item;
	}

	protected void populate(LinkedHashMap<Integer, String> values) {
		if (item == null) {
			Logger.w("tt9/ItemDropDown.populate", "Cannot populate a NULL item. Ignoring.");
			return;
		}

		this.values = values != null ? values : new LinkedHashMap<>();

		ArrayList<String> keys = new ArrayList<>();
		for (int key : this.values.keySet()) {
			keys.add(String.valueOf(key));
		}

		item.setEntryValues(keys.toArray(new CharSequence[0]));
		item.setEntries(this.values.values().toArray(new CharSequence[0]));
	}

	public ItemDropDown enableClickHandler() {
		if (item == null) {
			Logger.w("tt9/SectionKeymap.populateItem", "Cannot set a click listener a NULL item. Ignoring.");
			return this;
		}

		item.setOnPreferenceChangeListener(this::onClick);
		return this;
	}

	protected boolean onClick(Preference preference, Object newKey) {
		try {
			String previewValue = values.get(Integer.parseInt(newKey.toString()));
			((DropDownPreference) preference).setValue(newKey.toString());
			setPreview(previewValue);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private void setPreview(String value) {
		if (item != null) {
			item.setSummary(value);
		}
	}

	public ItemDropDown preview() {
		try {
			setPreview(values.get(Integer.parseInt(item.getValue())));
		} catch (NumberFormatException e) {
			setPreview("");
		}

		return this;
	}
}
