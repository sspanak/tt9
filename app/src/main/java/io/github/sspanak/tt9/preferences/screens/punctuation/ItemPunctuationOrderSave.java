package io.github.sspanak.tt9.preferences.screens.punctuation;

import androidx.preference.Preference;

import io.github.sspanak.tt9.preferences.items.ItemClickable;

public class ItemPunctuationOrderSave extends ItemClickable {
	public static final String NAME = "punctuation_order_save";
	private final Runnable clickHandler;

	public ItemPunctuationOrderSave(Preference item, Runnable clickHandler) {
		super(item);
		this.clickHandler = clickHandler;
	}

	@Override
	protected boolean onClick(Preference p) {
		if (clickHandler == null) {
			return false;
		}

		clickHandler.run();
		return true;
	}
}