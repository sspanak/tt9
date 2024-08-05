package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

class ItemSelectLanguage {
	public static final String NAME = "pref_languages";

	private final PreferencesActivity activity;
	private final Preference item;

	ItemSelectLanguage(PreferencesActivity activity, Preference item) {
		this.activity = activity;
		this.item = item;
	}

	public ItemSelectLanguage populate() {
		previewSelection();
		return this;
	}


	private void previewSelection() {
		if (item == null) {
			return;
		}

		item.setSummary(
			LanguageCollection.toString(LanguageCollection.getAll(activity, activity.getSettings().getEnabledLanguageIds(), true))
		);
	}
}
