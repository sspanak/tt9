package io.github.sspanak.tt9.preferences.items;

import androidx.preference.Preference;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.UI;

public class ItemSetDefaultGlobalKeyboard extends ItemClickable {
	private final PreferencesActivity activity;

	public ItemSetDefaultGlobalKeyboard(Preference item, PreferencesActivity prefs) {
		super(item);
		this.activity = prefs;
	}

	@Override
	protected boolean onClick(Preference p) {
		UI.showChangeKeyboardDialog(activity);
		return false;
	}
}
