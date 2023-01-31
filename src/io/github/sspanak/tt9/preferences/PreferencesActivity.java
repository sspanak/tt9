package io.github.sspanak.tt9.preferences;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.preferences.screens.MainSettingsScreen;
import io.github.sspanak.tt9.preferences.screens.HotkeysScreen;
import io.github.sspanak.tt9.ui.DictionaryLoadingBar;

public class PreferencesActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
	public SettingsStore settings;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		settings = new SettingsStore(this);
		applyTheme();

		DictionaryDb.init(this);
		DictionaryDb.normalizeWordFrequencies(settings);

		super.onCreate(savedInstanceState);
		validateFunctionKeys();
		buildScreen();
	}


	@Override
	public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
		// instantiate the new Fragment
		Fragment fragment;
		if (pref.getFragment() != null && pref.getFragment().contains("Hotkeys")) {
			fragment = new HotkeysScreen(this);
		} else {
			fragment = new MainSettingsScreen(this);
		}
		fragment.setArguments(pref.getExtras());

		// replace the existing Fragment with the new Fragment
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.preferences_container, fragment)
			.addToBackStack(null)
			.commit();

		return true;
	}


	private void applyTheme() {
		AppCompatDelegate.setDefaultNightMode(
			settings.getDarkTheme() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
		);
	}


	private void buildScreen() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true); // hide the "back" button, if visible
		}

		setContentView(R.layout.preferences_container);
		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.preferences_container, new MainSettingsScreen(this))
			.commit();
	}


	public void setScreenTitle(int title) {
		// set the title
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(title);
		}
	}


	private void validateFunctionKeys() {
		if (!settings.areFunctionKeysSet()) {
			settings.setDefaultKeys();
		}
	}


	public DictionaryLoadingBar getDictionaryProgressBar() {
		return DictionaryLoadingBar.getInstance(this);
	}


	public DictionaryLoader getDictionaryLoader() {
		return DictionaryLoader.getInstance(this);
	}
}
