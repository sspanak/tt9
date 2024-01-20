package io.github.sspanak.tt9.preferences.items;

import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.AsyncWordStore;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.SettingsStore;


public class ItemTruncateUnselected extends ItemTruncateAll {
	public static final String NAME = "dictionary_truncate_unselected";

	private final SettingsStore settings;


	public ItemTruncateUnselected(Preference item, PreferencesActivity context, SettingsStore settings, DictionaryLoader loader) {
		super(item, context, loader);
		this.settings = settings;
	}



	@Override
	protected boolean onClick(Preference p) {
		if (loader != null && loader.isRunning()) {
			return false;
		}

		ArrayList<Integer> unselectedLanguageIds = new ArrayList<>();
		ArrayList<Integer> selectedLanguageIds = settings.getEnabledLanguageIds();
		for (Language lang : LanguageCollection.getAll(activity, false)) {
			if (!selectedLanguageIds.contains(lang.getId())) {
				unselectedLanguageIds.add(lang.getId());
			}
		}

		onStartDeleting();
		AsyncWordStore.deleteWords(this::onFinishDeleting, unselectedLanguageIds);

		return true;
	}
}
