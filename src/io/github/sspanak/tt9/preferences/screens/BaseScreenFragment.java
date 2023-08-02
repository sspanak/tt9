package io.github.sspanak.tt9.preferences.screens;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

abstract class BaseScreenFragment extends PreferenceFragmentCompat {
	protected PreferencesActivity activity;


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


	abstract protected int getTitle();
	abstract protected int getXml();
	abstract protected void onCreate();
}
