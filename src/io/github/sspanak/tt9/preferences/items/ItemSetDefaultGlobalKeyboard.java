package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import androidx.preference.Preference;

import io.github.sspanak.tt9.preferences.PreferencesActivity;

public class ItemSetDefaultGlobalKeyboard extends ItemClickable {
	private final PreferencesActivity activity;

	public ItemSetDefaultGlobalKeyboard(Preference item, PreferencesActivity prefs) {
		super(item);
		this.activity = prefs;
	}

	@Override
	protected boolean onClick(Preference p) {
		((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
		return false;
	}
}
