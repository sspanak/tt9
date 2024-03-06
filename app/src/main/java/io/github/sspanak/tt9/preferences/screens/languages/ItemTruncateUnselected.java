package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.SettingsStore;


class ItemTruncateUnselected extends ItemTruncateAll {
	public static final String NAME = "dictionary_truncate_unselected";

	private final SettingsStore settings;


	ItemTruncateUnselected(Preference item, PreferencesActivity context, SettingsStore settings, Runnable onStart, Runnable onFinish) {
		super(item, context, onStart, onFinish);
		this.settings = settings;
	}


	@Override
	protected boolean onClick(Preference p) {
		ArrayList<Integer> unselectedLanguageIds = new ArrayList<>();
		ArrayList<Integer> selectedLanguageIds = settings.getEnabledLanguageIds();
		for (Language lang : LanguageCollection.getAll(activity, false)) {
			if (!selectedLanguageIds.contains(lang.getId())) {
				unselectedLanguageIds.add(lang.getId());
			}
		}

		onStartDeleting();
		WordStoreAsync.deleteWords(this::onFinishDeleting, unselectedLanguageIds);

		return true;
	}
}
