package io.github.sspanak.tt9.preferences.items;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.UI;


public class ItemTruncateAll extends ItemClickable {
	public static final String NAME = "dictionary_truncate";

	protected final PreferencesActivity activity;
	protected final DictionaryLoader loader;
	protected final ItemLoadDictionary loadItem;
	protected ItemClickable otherTruncateItem;

	public ItemTruncateAll(Preference item, ItemLoadDictionary loadItem, PreferencesActivity activity, DictionaryLoader loader) {
		super(item);
		this.activity = activity;
		this.loadItem = loadItem;
		this.loader = loader;
	}


	public ItemTruncateAll setOtherTruncateItem(ItemTruncateUnselected item) {
		this.otherTruncateItem = item;
		return this;
	}


	@Override
	protected boolean onClick(Preference p) {
		if (loader != null && loader.isRunning()) {
			loader.stop();
			loadItem.changeToLoadButton();
		}

		onStartDeleting();
		DictionaryDb.deleteWords(this::onFinishDeleting);

		return true;
	}


	protected void onStartDeleting() {
		if (otherTruncateItem != null) {
			otherTruncateItem.disable();
		}
		loadItem.disable();
		disable();
		item.setSummary(R.string.dictionary_truncating);
	}


	protected void onFinishDeleting() {
		activity.runOnUiThread(() -> {
			if (otherTruncateItem != null) {
				otherTruncateItem.enable();
			}
			loadItem.enable();
			item.setSummary("");
			enable();
			UI.toastFromAsync(activity, R.string.dictionary_truncated);
		});
	}
}
