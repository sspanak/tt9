package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import io.github.sspanak.tt9.preferences.items.ItemClickable;

class ItemDeleteCustomWords extends ItemClickable {
	final static String NAME = "screen_delete_words";
	ItemDeleteCustomWords(Preference item) { super(item); }
	@Override protected boolean onClick(Preference p) { return false; }
}
