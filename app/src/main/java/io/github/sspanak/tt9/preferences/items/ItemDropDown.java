package io.github.sspanak.tt9.preferences.items;

import androidx.annotation.Nullable;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class ItemDropDown {
	protected final DropDownPreference item;
	protected LinkedHashMap<String, String> values;

	public ItemDropDown(DropDownPreference item) {
		this.item = item;
	}


	public void add(int key, String value) {
		add(String.valueOf(key), value);
	}


	public ItemDropDown add(int key, int resId) {
		if (item == null) {
			Logger.w("ItemDropDown.add", "Cannot add option to a NULL item or context. Ignoring.");
			return this;
		}
		add(String.valueOf(key), item.getContext().getString(resId));
		return this;
	}


	public ItemDropDown add(String key, String value) {
		if (values == null) {
			values = new LinkedHashMap<>();
		}
		values.put(key, value);
		return this;
	}


	public ItemDropDown commitOptions() {
		populate(values);
		return this;
	}


	@Nullable
	public String get(String key) {
		if (values != null) {
			return values.get(key);
		}
		return null;
	}


	public ItemDropDown populate() {
		return this;
	}


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


	public void populatePercentRange(int start, int end, int step) {
		if (start < end && step > 0) {
			for (int i = start; i <= end; i += step) {
				add(i, i + " ％");
			}
		}
		commitOptions();
	}


	public ItemDropDown sort() {
		if (!DeviceInfo.AT_LEAST_ANDROID_7 || values.size() <= 1) {
			return this;
		}

		LinkedHashMap<String, String> sorted = new LinkedHashMap<>();
		values.entrySet().stream()
			.sorted(LinkedHashMap.Entry.comparingByValue())
			.forEachOrdered(e -> sorted.put(e.getKey(), e.getValue()));

		values = sorted;

		return this;
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


	@Nullable public String getValue() {
		return item != null ? item.getValue() : null;
	}
}
