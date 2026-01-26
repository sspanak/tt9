package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

record ItemSelectLanguage(PreferencesActivity activity, Preference item) {
	public static final String NAME = "pref_languages";

	public ItemSelectLanguage populate() {
		previewSelection();
		return this;
	}


	private void previewSelection() {
		if (item == null) {
			return;
		}

		item.setSummary(
			LanguageCollection.toString(LanguageCollection.getAll(activity.getSettings().getEnabledLanguageIds(), true))
		);
	}
}
