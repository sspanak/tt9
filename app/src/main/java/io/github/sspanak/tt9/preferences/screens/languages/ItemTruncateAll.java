package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.mindreader.MindReaderStore;
import io.github.sspanak.tt9.db.words.DictionaryDeleter;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.ui.UI;


class ItemTruncateAll extends ItemClickable {
	public static final String NAME = "dictionary_truncate";

	protected final PreferencesActivity activity;
	protected final DictionaryDeleter deleterOfWords;
	protected final MindReaderStore mindReaderStore;
	protected final Runnable onStart;
	private final Runnable onFinish;


	ItemTruncateAll(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item);
		this.activity = activity;
		this.deleterOfWords = DictionaryDeleter.getInstance(activity);
		this.mindReaderStore = new MindReaderStore(activity);
		this.onStart = onStart;
		this.onFinish = onFinish;
	}


	@Override
	protected boolean onClick(Preference p) {
		onStart.run();
		setBusy();
		delete(LanguageCollection.getAll(false));

		return true;
	}


	void refreshStatus() {
		if (deleterOfWords.isRunning()) {
			setBusy();
		} else {
			enable();
		}
	}


	protected void setBusy() {
		deleterOfWords.setOnFinish(this::onFinishDeleting);
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


	protected void delete(@NonNull ArrayList<Language> languages) {
		// mind reader operations only take milliseconds, so we can safely run it in parallel with
		// the word deletions and assume it will be done by the time we need to update the UI in
		// onFinishDeleting()
		mindReaderStore.truncate(languages, () -> {
			for (Language language : languages) {
				activity.getSettings().setMindReaderFactoryNgramsRevision(language, "");
			}
		});

		deleterOfWords.deleteLanguages(languages);
	}
}
