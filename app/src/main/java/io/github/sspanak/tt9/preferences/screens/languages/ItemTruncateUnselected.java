package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
		ArrayList<Language> unselectedLanguages = new ArrayList<>();
		Set<Integer> selectedLanguageIds = new HashSet<>(activity.getSettings().getEnabledLanguageIds());
		for (Language lang : LanguageCollection.getAll(false)) {
			if (!selectedLanguageIds.contains(lang.getId())) {
				unselectedLanguages.add(lang);
			}
		}

		setBusy();
		deleter.setOnFinish(this::onFinishDeleting);
		deleter.deleteLanguages(unselectedLanguages);

		return true;
	}
}
