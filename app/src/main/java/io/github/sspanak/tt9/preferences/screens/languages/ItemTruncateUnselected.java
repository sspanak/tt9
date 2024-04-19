package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;


class ItemTruncateUnselected extends ItemTruncateAll {
	public static final String NAME = "dictionary_truncate_unselected";


	ItemTruncateUnselected(Preference item, PreferencesActivity context, Runnable onStart, Runnable onFinish) {
		super(item, context, onStart, onFinish);
	}


	@Override
	protected boolean onClick(Preference p) {
		ArrayList<Integer> unselectedLanguageIds = new ArrayList<>();
		ArrayList<Integer> selectedLanguageIds = activity.getSettings().getEnabledLanguageIds();
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
