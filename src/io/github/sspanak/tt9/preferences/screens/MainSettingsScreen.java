package io.github.sspanak.tt9.preferences.screens;

import android.content.Intent;
import android.net.Uri;

import androidx.preference.Preference;

import java.util.regex.Pattern;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemLoadDictionary;
import io.github.sspanak.tt9.preferences.items.ItemSelectLanguage;
import io.github.sspanak.tt9.preferences.items.ItemSelectZeroKeyCharacter;
import io.github.sspanak.tt9.preferences.items.ItemToggleDarkTheme;
import io.github.sspanak.tt9.preferences.items.ItemTruncateDictionary;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

public class MainSettingsScreen extends BaseScreenFragment {
	private final Pattern releaseVersionRegex = Pattern.compile("^\\d+\\.\\d+$");

	public MainSettingsScreen() {
		init();
	}

	public MainSettingsScreen(PreferencesActivity activity) {
		init(activity);
	}


	@Override
	protected int getTitle() {
		return R.string.app_settings;
	}


	@Override
	protected int getXml() {
		return R.xml.prefs;
	}


	@Override
	public void onCreate() {
		createAboutSection();
		createAppearanceSection();
		createDictionarySection();
		createHelpSection();
		createPredictiveModeSection();
	}


	private void createDictionarySection() {
		ItemSelectLanguage multiSelect = new ItemSelectLanguage(
			findPreference(ItemSelectLanguage.NAME),
			activity.settings
		);
		multiSelect.populate().enableValidation();

		ItemLoadDictionary loadItem = new ItemLoadDictionary(
			findPreference(ItemLoadDictionary.NAME),
			activity,
			activity.settings,
			activity.getDictionaryLoader(),
			activity.getDictionaryProgressBar()
		);
		loadItem.enableClickHandler();

		ItemTruncateDictionary truncateItem = new ItemTruncateDictionary(
			findPreference(ItemTruncateDictionary.NAME),
			loadItem,
			activity,
			activity.getDictionaryLoader()
		);
		truncateItem.enableClickHandler();
	}


	private void createAppearanceSection() {
		(new ItemToggleDarkTheme(findPreference(ItemToggleDarkTheme.NAME))).enableToggleHandler();
	}


	private void createPredictiveModeSection() {
		(new ItemSelectZeroKeyCharacter(findPreference(ItemSelectZeroKeyCharacter.NAME), activity)).populate().activate();
	}


	private void createHelpSection() {
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
			Logger.w("tt9/MainSettingsScreen", "Could not set versioned help URL. Falling back to the default. " + e.getMessage());
		}
	}


	private void createAboutSection() {
		Preference vi = findPreference("version_info");
		if (vi != null) {
			vi.setSummary(BuildConfig.VERSION_FULL);
		}
	}
}
