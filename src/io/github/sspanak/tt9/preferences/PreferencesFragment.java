package io.github.sspanak.tt9.preferences;

import android.os.Bundle;

import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Arrays;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;

public class PreferencesFragment extends PreferenceFragmentCompat {
	private PreferencesActivity activity;

	public PreferencesFragment() {
		super();
		init();
	}

	public PreferencesFragment(PreferencesActivity activity) {
		super();
		this.activity = activity;
		init();
	}


	private void init() {
		if (activity == null) {
			activity = (PreferencesActivity) getActivity();
		}
	}


	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.prefs, rootKey);

		if (activity == null) {
			Logger.w(
				"tt9/PreferencesFragment",
				"Starting up without an Activity. Preference Items will not be fully initialized."
			);
			return;
		}

		createDictionarySection();
		createAppearanceSection();
		createKeymapSection();
		createAboutSection();
	}


	private void createDictionarySection() {
		ItemSelectLanguage multiSelect = new ItemSelectLanguage(
			findPreference(ItemSelectLanguage.NAME),
			SettingsStore.getInstance()
		);
		multiSelect.populate().enableValidation();

		ItemLoadDictionary loadItem = new ItemLoadDictionary(
			findPreference(ItemLoadDictionary.NAME),
			activity,
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


	private void createKeymapSection() {
		DropDownPreference[] dropDowns = {
			findPreference(SectionKeymap.ITEM_ADD_WORD),
			findPreference(SectionKeymap.ITEM_BACKSPACE),
			findPreference(SectionKeymap.ITEM_NEXT_INPUT_MODE),
			findPreference(SectionKeymap.ITEM_NEXT_LANGUAGE),
			findPreference(SectionKeymap.ITEM_SHOW_SETTINGS),
		};
		SectionKeymap section = new SectionKeymap(Arrays.asList(dropDowns), activity, SettingsStore.getInstance());
		section.populate().activate();

		(new ItemResetKeys(findPreference(ItemResetKeys.NAME), activity, section, SettingsStore.getInstance()))
			.enableClickHandler();
	}

	private void createAboutSection() {
		Preference vi = findPreference("version_info");
		if (vi != null) {
			vi.setSummary(BuildConfig.VERSION_NAME);
		}
	}
}
