package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.words.DictionaryDeleter;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.ui.UI;


class ItemTruncateAll extends ItemClickable {
	public static final String NAME = "dictionary_truncate";

	protected final PreferencesActivity activity;
	protected final DictionaryDeleter deleter;
	private final Runnable onStart;
	private final Runnable onFinish;


	ItemTruncateAll(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item);
		this.activity = activity;
		this.deleter = DictionaryDeleter.getInstance(activity);
		this.onStart = onStart;
		this.onFinish = onFinish;

		refreshStatus();
	}


	@Override
	protected boolean onClick(Preference p) {
		onStart.run();
		setBusy();
		deleter.deleteLanguages(LanguageCollection.getAll(false));

		return true;
	}


	void refreshStatus() {
		if (deleter.isRunning()) {
			setBusy();
		} else {
			enable();
		}
	}


	protected void setBusy() {
		deleter.setOnFinish(this::onFinishDeleting);
		item.setSummary(R.string.dictionary_truncating);
		disable();
	}


	public void enable() {
		super.enable();
		item.setSummary("");
	}


	protected void onFinishDeleting() {
		activity.runOnUiThread(() -> {
			onFinish.run();
			enable();
			UI.toastFromAsync(activity, R.string.dictionary_truncated);
		});
	}
}
