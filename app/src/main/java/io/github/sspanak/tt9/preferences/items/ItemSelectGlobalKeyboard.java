package io.github.sspanak.tt9.preferences.items;

import android.content.Intent;
import android.provider.Settings;

import androidx.preference.Preference;

import io.github.sspanak.tt9.preferences.PreferencesActivity;

public class ItemSelectGlobalKeyboard extends ItemClickable {
	private final Intent clickIntent;
	private final PreferencesActivity activity;

	public ItemSelectGlobalKeyboard(Preference item, PreferencesActivity prefs) {
		super(item);
		this.activity = prefs;

		clickIntent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
		clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	@Override
	protected boolean onClick(Preference p) {
		activity.startActivity(clickIntent);
		return false;
	}
}
