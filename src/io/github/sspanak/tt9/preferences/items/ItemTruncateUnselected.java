package io.github.sspanak.tt9.preferences.items;

import android.content.Context;

import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.ui.UI;


public class ItemTruncateUnselected extends ItemClickable {
	public static final String NAME = "dictionary_truncate_unselected";

	private final Context context;
	private final DictionaryLoader loader;
	private final ItemLoadDictionary loadItem;
	private final SettingsStore settings;


	public ItemTruncateUnselected(Preference item, ItemLoadDictionary loadItem, Context context, SettingsStore settings, DictionaryLoader loader) {
		super(item);
		this.context = context;
		this.loadItem = loadItem;
		this.settings = settings;
		this.loader = loader;
	}


	@Override
	protected boolean onClick(Preference p) {
		if (loader != null && loader.isRunning()) {
			loader.stop();
			loadItem.changeToLoadButton();
		}

		ArrayList<Integer> unselectedLanguageIds = new ArrayList<>();
		ArrayList<Integer> selectedLanguageIds = settings.getEnabledLanguageIds();
		for (Language lang : LanguageCollection.getAll(false)) {
			if (!selectedLanguageIds.contains(lang.getId())) {
				unselectedLanguageIds.add(lang.getId());
			}
		}

		DictionaryDb.deleteWords(
			() -> UI.toastFromAsync(context, R.string.dictionary_truncated),
			unselectedLanguageIds
		);

		return true;
	}
}
