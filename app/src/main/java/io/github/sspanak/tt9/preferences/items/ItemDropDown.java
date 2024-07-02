package io.github.sspanak.tt9.preferences.items;

import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import io.github.sspanak.tt9.util.Logger;

abstract public class ItemDropDown {
	protected final DropDownPreference item;
	private LinkedHashMap<String, String> values;

	public ItemDropDown(DropDownPreference item) {
		this.item = item;
	}

	protected void populateIntegers(LinkedHashMap<Integer, String> values) {
		LinkedHashMap<String, String> stringifiedValues = new LinkedHashMap<>();
		if (values != null) {
			for (Integer key : values.keySet()) {
				stringifiedValues.put(String.valueOf(key), values.get(key));
			}
		}

		populate(stringifiedValues);
	}

	abstract public ItemDropDown populate();

	protected void populate(LinkedHashMap<String, String> values) {
		if (item == null) {
			Logger.w("ItemDropDown.populate", "Cannot populate a NULL item. Ignoring.");
			return;
		}

		this.values = values != null ? values : new LinkedHashMap<>();
		ArrayList<String> keys = new ArrayList<>(this.values.keySet());

		item.setEntryValues(keys.toArray(new CharSequence[0]));
		item.setEntries(this.values.values().toArray(new CharSequence[0]));
	}

	public ItemDropDown enableClickHandler() {
		if (item != null) {
			item.setOnPreferenceChangeListener(this::onClick);
		}

		return this;
	}

	protected boolean onClick(Preference preference, Object newKey) {
		String previewValue = values.get(newKey.toString());
		((DropDownPreference) preference).setValue(newKey.toString());
		setPreview(previewValue);

		return true;
	}

	private void setPreview(String value) {
		if (item != null) {
			item.setSummary(value);
		}
	}

	public ItemDropDown preview() {
		try {
			setPreview(values.get(item.getValue()));
		} catch (Exception e) {
			setPreview("");
		}

		return this;
	}

	public ItemDropDown setValue(String value) {
		if (item != null) {
			item.setValue(value);
		}
		return this;
	}
}
