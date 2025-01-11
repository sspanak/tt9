package io.github.sspanak.tt9.preferences.screens;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.ScreenPreferencesList;
import io.github.sspanak.tt9.preferences.settings.SettingsUI;
import io.github.sspanak.tt9.util.Logger;

abstract public class BaseScreenFragment extends PreferenceFragmentCompat {
	protected PreferencesActivity activity;
	private ScreenPreferencesList preferencesList;


	protected void init(PreferencesActivity activity) {
		this.activity = activity;
		init();
	}


	protected void init() {
		if (activity == null) {
			activity = (PreferencesActivity) getActivity();
			setScreenTitle();
		}
	}


	private void setScreenTitle() {
		if (activity != null) {
			activity.setScreenTitle(getTitle());
		}
	}


	private void initPreferencesList() {
		if (preferencesList == null) {
			preferencesList = new ScreenPreferencesList(getPreferenceScreen());
		}
	}


	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setHasOptionsMenu(true); // enable "back" in "onOptionsItemSelected()"
		setPreferencesFromResource(getXml(), rootKey);

		if (activity == null) {
			Logger.w(
				"MainSettingsScreen",
				"Starting up without an Activity. Preference Items will not be fully initialized."
			);
			return;
		}

		onCreate();
	}


	@Override
	public void onResume() {
		super.onResume();
		setScreenTitle();
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == android.R.id.home && activity != null && !super.onOptionsItemSelected(item)) {
			activity.onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	public int getPreferenceCount() {
		initPreferencesList();
		return preferencesList.size();
	}


	public void resetFontSize(boolean reloadList) {
		initPreferencesList();
		preferencesList.getAll(reloadList, true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			preferencesList.setFontSize(SettingsUI.FONT_SIZE_DEFAULT);
		} else {
			preferencesList.setFontSize(activity.getSettings().getSettingsFontSize());
		}

	}


	abstract public String getName();
	abstract protected int getTitle();
	abstract protected int getXml();
	abstract protected void onCreate();

	public void onBackPressed() {}
}
