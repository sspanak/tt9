package io.github.sspanak.tt9.preferences.items;

import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.UI;


public class ItemTruncateAll extends ItemClickable {
	public static final String NAME = "dictionary_truncate";

	protected final PreferencesActivity activity;
	private final Runnable onStart;
	private final Runnable onFinish;


	public ItemTruncateAll(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item);
		this.activity = activity;
		this.onStart = onStart;
		this.onFinish = onFinish;
	}


	@Override
	protected boolean onClick(Preference p) {
		onStartDeleting();
		ArrayList<Integer> languageIds = new ArrayList<>();
		for (Language lang : LanguageCollection.getAll(activity, false)) {
			languageIds.add(lang.getId());
		}
		WordStoreAsync.deleteWords(this::onFinishDeleting, languageIds);

		return true;
	}


	protected void onStartDeleting() {
		onStart.run();
		disable();
		item.setSummary(R.string.dictionary_truncating);
	}


	protected void onFinishDeleting() {
		activity.runOnUiThread(() -> {
			onFinish.run();
			item.setSummary("");
			enable();
			UI.toastFromAsync(activity, R.string.dictionary_truncated);
		});
	}
}
