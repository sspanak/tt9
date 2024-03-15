package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.MultiSelectListPreference;

import java.util.ArrayList;
import java.util.HashSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.UI;

class ItemSelectLanguage {
	public static final String NAME = "pref_languages";

	private final PreferencesActivity activity;
	private final MultiSelectListPreference item;

	ItemSelectLanguage(PreferencesActivity activity, MultiSelectListPreference multiSelect) {
		this.activity = activity;
		this.item = multiSelect;
	}

	public ItemSelectLanguage populate() {
		if (item == null) {
			return this;
		}

		ArrayList<Language> languages = LanguageCollection.getAll(activity, true);
		if (languages.isEmpty()) {
			UI.alert(activity, R.string.error, R.string.failed_loading_language_definitions);
			// do not return, the MultiSelect component requires arrays, even if empty, otherwise it crashes
		}

		ArrayList<CharSequence> values = new ArrayList<>();
		for (Language l : languages) {
			values.add(String.valueOf(l.getId()));
		}

		ArrayList<String> keys = new ArrayList<>();
		for (Language l : languages) {
			keys.add(l.getName());
		}

		item.setEntries(keys.toArray(new CharSequence[0]));
		item.setEntryValues(values.toArray(new CharSequence[0]));
		item.setValues(activity.getSettings().getEnabledLanguagesIdsAsStrings());
		previewSelection();

		return this;
	}


	public void enableValidation() {
		if (item == null) {
			return;
		}

		item.setOnPreferenceChangeListener((preference, newValue) -> {
			@SuppressWarnings("unchecked") HashSet<String> newLanguages = (HashSet<String>) newValue;
			if (newLanguages.isEmpty()) {
				newLanguages.add("1");
			}

			activity.getSettings().saveEnabledLanguageIds(newLanguages);
			item.setValues(activity.getSettings().getEnabledLanguagesIdsAsStrings());
			previewSelection();

			// we validate and save manually above, so "false" disables automatic save
			return false;
		});

	}


	private void previewSelection() {
		item.setSummary(
			LanguageCollection.toString(LanguageCollection.getAll(activity, activity.getSettings().getEnabledLanguageIds(), true))
		);
	}
}
