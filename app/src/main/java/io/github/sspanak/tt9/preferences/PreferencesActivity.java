package io.github.sspanak.tt9.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.db.words.LegacyDb;
import io.github.sspanak.tt9.ime.helpers.InputModeValidator;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.preferences.screens.UsageStatsScreen;
import io.github.sspanak.tt9.preferences.screens.appearance.AppearanceScreen;
import io.github.sspanak.tt9.preferences.screens.debug.DebugScreen;
import io.github.sspanak.tt9.preferences.screens.deleteWords.DeleteWordsScreen;
import io.github.sspanak.tt9.preferences.screens.fnKeyOrder.FnKeyOrderScreen;
import io.github.sspanak.tt9.preferences.screens.hotkeys.HotkeysScreen;
import io.github.sspanak.tt9.preferences.screens.keypad.KeyPadScreen;
import io.github.sspanak.tt9.preferences.screens.languageSelection.LanguageSelectionScreen;
import io.github.sspanak.tt9.preferences.screens.languages.LanguagesScreen;
import io.github.sspanak.tt9.preferences.screens.main.MainSettingsScreen;
import io.github.sspanak.tt9.preferences.screens.modeAbc.ModeAbcScreen;
import io.github.sspanak.tt9.preferences.screens.modePredictive.ModePredictiveScreen;
import io.github.sspanak.tt9.preferences.screens.punctuation.PunctuationScreen;
import io.github.sspanak.tt9.preferences.screens.setup.SetupScreen;
import io.github.sspanak.tt9.ui.ActivityWithNavigation;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.SystemSettings;

public class PreferencesActivity extends ActivityWithNavigation implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getSettings();
		applyTheme();
		Logger.setLevel(settings.getLogLevel());

		LanguageCollection.init(this);
		try (LegacyDb db = new LegacyDb(this)) { db.clear(); }
		DataStore.init(this);

		InputModeValidator.validateEnabledLanguages(settings.getEnabledLanguageIds());
		validateFunctionKeys();

		super.onCreate(savedInstanceState);

		// changing the theme causes onCreate(), which displays the MainSettingsScreen,
		// but leaves the old "back" history, which is no longer valid,
		// so we must reset it
		getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		buildLayout();
	}


	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		preventEdgeToEdge(findViewById(R.id.preferences_container));
	}


	@Override
	public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
		BaseScreenFragment fragment = getScreen((getScreenName(pref)));
		fragment.setArguments(pref.getExtras());
		displayScreen(fragment, true);
		return true;
	}


	@Override
	protected void onResume() {
		super.onResume();

		if (!SystemSettings.isTT9Enabled(this)) {
			return;
		}

		Intent intent = getIntent();
		String screenName = intent != null ? intent.getStringExtra("screen") : null;
		screenName = screenName != null ? screenName : "";

		BaseScreenFragment screen = getScreen(screenName);

		if (screen.getName().equals(screenName)) {
			displayScreen(screen, false);
		}
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getOnBackPressedDispatcher().onBackPressed();
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void selectOption(int position, boolean click) {
		// for convenience, scroll to the bottom on 0-key click
		try {
			if (position == 0) {
				position = getOptionsCount.call();
				resetKeyRepeat(); // ... but do not activate the last option on double click
			}
		}
		catch (Exception ignore) {}

		super.selectOption(position, click);
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
	private BaseScreenFragment getScreen(@Nullable String name) {
		if (name == null) {
			return new MainSettingsScreen(this);
		}

		return switch (name) {
			case AppearanceScreen.NAME -> new AppearanceScreen(this);
			case DebugScreen.NAME -> new DebugScreen(this);
			case DeleteWordsScreen.NAME -> new DeleteWordsScreen(this);
			case FnKeyOrderScreen.NAME -> new FnKeyOrderScreen(this);
			case HotkeysScreen.NAME -> new HotkeysScreen(this);
			case KeyPadScreen.NAME -> new KeyPadScreen(this);
			case LanguagesScreen.NAME -> new LanguagesScreen(this);
			case LanguageSelectionScreen.NAME -> new LanguageSelectionScreen(this);
			case ModePredictiveScreen.NAME -> new ModePredictiveScreen(this);
			case ModeAbcScreen.NAME -> new ModeAbcScreen(this);
			case PunctuationScreen.NAME -> new PunctuationScreen(this);
			case SetupScreen.NAME -> new SetupScreen(this);
			case UsageStatsScreen.NAME -> new UsageStatsScreen(this);
			default -> new MainSettingsScreen(this);
		};
	}


	/**
	 * displayScreen
	 * Replaces the currently displayed screen fragment with a new one.
	 */
	private void displayScreen(BaseScreenFragment screen, boolean addToBackStack) {
		getOptionsCount = screen::getPreferenceCount;

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		transaction.replace(R.id.preferences_container, screen);
		if (addToBackStack) {
			transaction.addToBackStack(screen.getClass().getSimpleName());
		}

		transaction.commit();
	}


	public void displayScreen(@NonNull String screenName) {
		displayScreen(getScreen(screenName), true);
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
			settings.setDefaultKeys();
		}
	}
}
