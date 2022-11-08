package io.github.sspanak.tt9.preferences;

import androidx.preference.Preference;

public abstract class ItemClickable {
	protected final Preference item;

	ItemClickable(Preference item) {
		this.item = item;
	}

	public void enableClickHandler() {
		item.setOnPreferenceClickListener(this::onClick);
	}

	abstract protected boolean onClick(Preference p);
}
