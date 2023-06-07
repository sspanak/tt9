package io.github.sspanak.tt9.preferences.items;

import androidx.preference.Preference;

import io.github.sspanak.tt9.Logger;

abstract class ItemClickable {
	protected final int CLICK_DEBOUNCE_TIME = 250;
	private long lastClickTime = 0;

	protected final Preference item;


	ItemClickable(Preference item) {
		this.item = item;
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


	/**
	 * debounceClick
	 * Protection against faulty devices, that sometimes send two (or more) click events
	 * per a single key press.
	 *
	 * My smashed Qin F21 Pro+ occasionally does this, if I press the keys hard.
	 * There were reports the same happens on Kyocera KYF31, causing absolutely undesirable side effects.
	 * @see: <a href="https://github.com/sspanak/tt9/issues/117">...</a>
	 */
	protected boolean debounceClick(Preference p) {
		long now = System.currentTimeMillis();
		if (now - lastClickTime < CLICK_DEBOUNCE_TIME) {
			Logger.d("debounceClick", "Preference click debounced.");
			return true;
		}
		lastClickTime = now;

		return onClick(p);
	}


	abstract protected boolean onClick(Preference p);
}
