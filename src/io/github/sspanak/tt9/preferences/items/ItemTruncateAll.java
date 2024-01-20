package io.github.sspanak.tt9.preferences.items;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.AsyncWordStore;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.UI;


public class ItemTruncateAll extends ItemClickable {
	public static final String NAME = "dictionary_truncate";

	protected final PreferencesActivity activity;
	protected final DictionaryLoader loader;


	public ItemTruncateAll(Preference item, PreferencesActivity activity, DictionaryLoader loader) {
		super(item);
		this.activity = activity;
		this.loader = loader;
	}


	@Override
	protected boolean onClick(Preference p) {
		if (loader != null && loader.isRunning()) {
			return false;
		}

		onStartDeleting();
		AsyncWordStore.deleteWords(activity.getApplicationContext(), this::onFinishDeleting);

		return true;
	}


	protected void onStartDeleting() {
		disableOtherItems();
		disable();
		item.setSummary(R.string.dictionary_truncating);
	}


	protected void onFinishDeleting() {
		activity.runOnUiThread(() -> {
			enableOtherItems();
			item.setSummary("");
			enable();
			UI.toastFromAsync(activity, R.string.dictionary_truncated);
		});
	}
}
