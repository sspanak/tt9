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
		addHelpLink();
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


	private void addHelpLink() {
		try {
			if (!releaseVersionRegex.matcher(BuildConfig.VERSION_NAME).find()) {
				throw new Exception("VERSION_NAME does not match: \\d+.\\d+");
			}

			Preference helpSection = findPreference("help");
			if (helpSection == null) {
				throw new Exception("Could not find Help Preference");
			}

			String majorVersion = BuildConfig.VERSION_NAME.substring(0, BuildConfig.VERSION_NAME.indexOf('.'));
			String versionedHelpUrl = getString(R.string.help_url).replace("blob/master", "blob/v" + majorVersion + ".0");

			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			intent.setData(Uri.parse(versionedHelpUrl));
			helpSection.setIntent(intent);
		} catch (Exception e) {
			Logger.w("MainSettingsScreen", "Could not set versioned help URL. Falling back to the default. " + e.getMessage());
		}
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
