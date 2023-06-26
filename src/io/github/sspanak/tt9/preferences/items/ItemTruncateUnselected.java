package io.github.sspanak.tt9.preferences.items;

import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.SettingsStore;


public class ItemTruncateUnselected extends ItemTruncateAll {
	public static final String NAME = "dictionary_truncate_unselected";

	private final SettingsStore settings;


	public ItemTruncateUnselected(Preference item, ItemLoadDictionary loadItem, PreferencesActivity context, SettingsStore settings, DictionaryLoader loader) {
		super(item, loadItem, context, loader);
		this.settings = settings;
	}


	public ItemTruncateUnselected setOtherTruncateItem(ItemTruncateAll otherTruncateItem) {
		this.otherTruncateItem = otherTruncateItem;
		return this;
	}


	@Override
	protected boolean onClick(Preference p) {
		if (loader != null && loader.isRunning()) {
			loader.stop();
			loadItem.changeToLoadButton();
		}

		ArrayList<Integer> unselectedLanguageIds = new ArrayList<>();
		ArrayList<Integer> selectedLanguageIds = settings.getEnabledLanguageIds();
		for (Language lang : LanguageCollection.getAll(activity, false)) {
			if (!selectedLanguageIds.contains(lang.getId())) {
				unselectedLanguageIds.add(lang.getId());
			}
		}

		onStartDeleting();
		DictionaryDb.deleteWords(this::onFinishDeleting, unselectedLanguageIds);

		return true;
	}
}
