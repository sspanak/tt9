package io.github.sspanak.tt9.preferences.items;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

abstract public class ItemSwitch {
	protected final SwitchPreferenceCompat item;

	public ItemSwitch(SwitchPreferenceCompat item) {
		this.item = item;
	}

	public ItemSwitch enableClickHandler() {
		if (item != null) {
			item.setOnPreferenceChangeListener(this::onClick);
		}

		return this;
	}

	protected boolean onClick(Preference p, Object newValue) {
		return true;
	}

	public ItemSwitch populate() {
		item.setChecked(getDefaultValue());
		return this;
	}

	abstract protected boolean getDefaultValue();
}
