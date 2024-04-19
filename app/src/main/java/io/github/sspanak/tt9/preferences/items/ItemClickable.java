package io.github.sspanak.tt9.preferences.items;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

abstract public class ItemClickable {
	private long lastClickTime = 0;

	protected final Preference item;

	public ItemClickable(Preference item) {
		this.item = item;
	}



	public static void disableAll(@NonNull ArrayList<ItemClickable> items) {
		for (ItemClickable i : items) {
			i.disable();
		}
	}


	public static void enableAll(@NonNull ArrayList<ItemClickable> items) {
		for (ItemClickable i : items) {
			i.enable();
		}
	}


	public static void disableOthers(@NonNull ArrayList<ItemClickable> items, @NonNull ItemClickable exclude) {
		for (ItemClickable i : items) {
			if (i != exclude) {
				i.disable();
			}
		}
	}


	public static void enableAllClickHandlers(@NonNull ArrayList<ItemClickable> items) {
		for (ItemClickable i : items) {
			i.enableClickHandler();
		}
	}


	public void disable() {
		item.setEnabled(false);
	}


	public void enable() {
		item.setEnabled(true);
	}


	public void enableClickHandler() {
		item.setOnPreferenceClickListener(this::debounceClick);
	}


	protected boolean debounceClick(Preference p) {
		long now = System.currentTimeMillis();
		if (now - lastClickTime < SettingsStore.PREFERENCES_CLICK_DEBOUNCE_TIME) {
			Logger.d("debounceClick", "Preference click debounced.");
			return true;
		}
		lastClickTime = now;

		return onClick(p);
	}


	abstract protected boolean onClick(Preference p);
}
