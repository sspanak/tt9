package io.github.sspanak.tt9.preferences;

import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.ime.helpers.GlobalKeyboardSettings;
import io.github.sspanak.tt9.ime.helpers.InputModeValidator;
import io.github.sspanak.tt9.preferences.helpers.Hotkeys;
import io.github.sspanak.tt9.preferences.screens.AppearanceScreen;
import io.github.sspanak.tt9.preferences.screens.DebugScreen;
import io.github.sspanak.tt9.preferences.screens.DictionariesScreen;
import io.github.sspanak.tt9.preferences.screens.HotkeysScreen;
import io.github.sspanak.tt9.preferences.screens.KeyPadScreen;
import io.github.sspanak.tt9.preferences.screens.MainSettingsScreen;
import io.github.sspanak.tt9.preferences.screens.SetupScreen;
import io.github.sspanak.tt9.ui.DictionaryLoadingBar;

public class PreferencesActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
	public SettingsStore settings;
	public GlobalKeyboardSettings globalKeyboardSettings;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		globalKeyboardSettings = new GlobalKeyboardSettings(this, (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
		settings = new SettingsStore(this);
		applyTheme();
		Logger.enableDebugLevel(settings.getDebugLogsEnabled());

		DictionaryDb.init(this);
		DictionaryDb.normalizeWordFrequencies(settings);

		InputModeValidator.validateEnabledLanguages(this, settings.getEnabledLanguageIds());
		validateFunctionKeys();

		super.onCreate(savedInstanceState);

		// changing the theme causes onCreate(), which displays the MainSettingsScreen,
		// but leaves the old "back" history, which is no longer valid,
		// so we must reset it
		getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		buildLayout();
	}


	@Override
	public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
		Fragment fragment = getScreen((getScreenName(pref)));
		fragment.setArguments(pref.getExtras());
		displayScreen(fragment, true);
		return true;
	}


	/**
	 * getScreenName
	 * Determines the name of the screen for the given preference, as defined in the preference's "fragment" attribute.
	 * Expected format: "current.package.name.screens.SomeNameScreen"
	 */
	private String getScreenName(@NonNull Preference pref) {
		String screenClassName = pref.getFragment();
		return screenClassName != null ? screenClassName.replaceFirst("^.+?([^.]+)Screen$", "$1") : "";
	}


	/**
	 * getScreen
	 * Finds a screen fragment by name. If there is no fragment with such name, the main screen
	 * fragment will be returned.
	 */
	private Fragment getScreen(String name) {
		switch (name) {
			case "Appearance":
				return new AppearanceScreen(this);
			case "Debug":
				return new DebugScreen(this);
			case "Dictionaries":
				return new DictionariesScreen(this);
			case "Hotkeys":
				return new HotkeysScreen(this);
			case "KeyPad":
				return new KeyPadScreen(this);
			case "Setup":
				return new SetupScreen(this);
			default:
				return new MainSettingsScreen(this);
		}
	}


	/**
	 * displayScreen
	 * Replaces the currently displayed screen fragment with a new one.
	 */
	private void displayScreen(Fragment screen, boolean addToBackStack) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		transaction.replace(R.id.preferences_container, screen);
		if (addToBackStack) {
			transaction.addToBackStack(screen.getClass().getSimpleName());
		}

		transaction.commit();
	}


	private void buildLayout() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true); // hide the "back" button, if visible
		}

		setContentView(R.layout.preferences_container);
		displayScreen(getScreen("default"), false);
	}


	public void setScreenTitle(int title) {
		// set the title
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(title);
		}
	}


	private void applyTheme() {
		AppCompatDelegate.setDefaultNightMode(settings.getTheme());
	}


	private void validateFunctionKeys() {
		if (settings.areHotkeysInitialized()) {
			Hotkeys.setDefault(settings);
		}
	}


	public DictionaryLoadingBar getDictionaryProgressBar() {
		return DictionaryLoadingBar.getInstance(this);
	}


	public DictionaryLoader getDictionaryLoader() {
		return DictionaryLoader.getInstance(this);
	}
}
