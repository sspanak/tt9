package io.github.sspanak.tt9.preferences.items;

import android.content.Context;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.ui.UI;


public class ItemTruncateAll extends ItemClickable {
	public static final String NAME = "dictionary_truncate";

	private final Context context;
	private final DictionaryLoader loader;
	private final ItemLoadDictionary loadItem;


	public ItemTruncateAll(Preference item, ItemLoadDictionary loadItem, Context context, DictionaryLoader loader) {
		super(item);
		this.context = context;
		this.loadItem = loadItem;
		this.loader = loader;
	}


	@Override
	protected boolean onClick(Preference p) {
		if (loader != null && loader.isRunning()) {
			loader.stop();
			loadItem.changeToLoadButton();
		}

		DictionaryDb.deleteWords(() -> UI.toastFromAsync(context, R.string.dictionary_truncated));

		return true;
	}
}
