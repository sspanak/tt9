package io.github.sspanak.tt9.preferences.screens;

import androidx.preference.Preference;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemLoadDictionary;
import io.github.sspanak.tt9.preferences.items.ItemSelectLanguage;
import io.github.sspanak.tt9.preferences.items.ItemToggleDarkTheme;
import io.github.sspanak.tt9.preferences.items.ItemTruncateDictionary;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

public class MainSettingsScreen extends BaseScreenFragment {
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
		createDictionarySection();
		createAppearanceSection();
		createAboutSection();
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


	private void createAboutSection() {
		Preference vi = findPreference("version_info");
		if (vi != null) {
			vi.setSummary(BuildConfig.VERSION_FULL);
		}
	}
}
