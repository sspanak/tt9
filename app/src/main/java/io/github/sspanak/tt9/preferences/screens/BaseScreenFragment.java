package io.github.sspanak.tt9.preferences.screens;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.util.Logger;

abstract public class BaseScreenFragment extends PreferenceFragmentCompat {
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


	public int getPreferenceCount() {
		PreferenceScreen screen = getPreferenceScreen();

		int count = 0;
		for (int i = screen.getPreferenceCount(); i > 0; i--) {
			Preference pref = screen.getPreference(i - 1);
			if (pref.isVisible()) {
				count += pref instanceof PreferenceCategory ? ((PreferenceCategory) pref).getPreferenceCount() : 1;
			}
		}

		return count;
	}


	abstract public String getName();
	abstract protected int getTitle();
	abstract protected int getXml();
	abstract protected void onCreate();
}
