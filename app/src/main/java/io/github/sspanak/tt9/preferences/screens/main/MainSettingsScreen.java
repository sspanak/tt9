package io.github.sspanak.tt9.preferences.screens.main;

import android.content.Intent;
import android.net.Uri;

import androidx.preference.Preference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.SystemSettings;

public class MainSettingsScreen extends BaseScreenFragment {
	final public static String NAME = "Main";
	private final Pattern releaseVersionRegex = Pattern.compile("^\\d+\\.\\d+$");

	public MainSettingsScreen() { init(); }
	public MainSettingsScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.app_settings;}
	@Override protected int getXml() { return R.xml.prefs; }


	@Override
	public void onCreate() {
		createSettingsSection();
		createAboutSection();
		resetFontSize(false);
	}


	@Override
	public void onResume() {
		init(); // changing the theme recreates the PreferencesActivity, making "this.activity" NULL, so we reinitialize it.
		super.onResume();
		createSettingsSection();
		resetFontSize(false);
	}


	private void createAboutSection() {
		Preference donate = findPreference("donate_link");
		if (donate != null) {
			String appName = getString(R.string.app_name_short);
			String url = getString(R.string.donate_url_short);
			donate.setSummary(getString(R.string.donate_summary, appName, url));
		}

		ItemVersionInfo debugOptions = new ItemVersionInfo(findPreference(ItemVersionInfo.NAME), activity);
		debugOptions.populate().enableClickHandler();
	}


	private void createSettingsSection() {
		boolean isTT9On = SystemSettings.isTT9Enabled(activity);

		Preference gotoSetup = findPreference("screen_setup");
		if (gotoSetup != null) {
			gotoSetup.setSummary(isTT9On ? "" : activity.getString(R.string.setup_click_here_to_enable));
		}

		ArrayList<Preference> screens = new ArrayList<>(Arrays.asList(
			findPreference("screen_appearance"),
			findPreference("screen_keypad"),
			findPreference("screen_languages")
		));

		for (Preference goToScreen : screens) {
			if (goToScreen != null) {
				goToScreen.setEnabled(isTT9On || activity.getSettings().getDemoMode());
			}
		}
	}
}
