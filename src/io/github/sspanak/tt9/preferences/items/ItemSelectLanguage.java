package io.github.sspanak.tt9.preferences.items;

import androidx.preference.MultiSelectListPreference;

import java.util.ArrayList;
import java.util.HashSet;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class ItemSelectLanguage {
	public static final String NAME = "pref_languages";

	private final SettingsStore settings;
	private final MultiSelectListPreference item;

	public ItemSelectLanguage(MultiSelectListPreference multiSelect, SettingsStore settings) {
		this.item = multiSelect;
		this.settings = settings;
	}

	public ItemSelectLanguage populate() {
		if (item == null) {
			return this;
		}

		ArrayList<Language> languages = LanguageCollection.getAll(true);

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
		item.setValues(settings.getEnabledLanguagesIdsAsStrings());
		previewSelection();

		return this;
	}


	public void enableValidation() {
		if (item == null) {
			return;
		}

		item.setOnPreferenceChangeListener((preference, newValue) -> {
			HashSet<String> newLanguages = (HashSet<String>) newValue;
			if (newLanguages.size() == 0) {
				newLanguages.add("1");
			}

			settings.saveEnabledLanguageIds(newLanguages);
			item.setValues(settings.getEnabledLanguagesIdsAsStrings());
			previewSelection();

			// we validate and save manually above, so "false" disables automatic save
			return false;
		});

	}


	private void previewSelection() {
		item.setSummary(
			LanguageCollection.toString(LanguageCollection.getAll(settings.getEnabledLanguageIds(), true))
		);
	}
}
