package io.github.sspanak.tt9.preferences.screens.languages;

import android.os.Bundle;

import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.notifications.DictionaryLoadingBar;


class ItemLoadDictionary extends ItemClickable {
	public final static String NAME = "dictionary_load";

	private final PreferencesActivity activity;
	private final Runnable onStart;
	private final Runnable onFinish;

	private final DictionaryLoader loader;
	private final DictionaryLoadingBar progressBar;


	ItemLoadDictionary(Preference item, PreferencesActivity context, Runnable onStart, Runnable onFinish) {
		super(item);

		this.activity = context;
		this.loader = DictionaryLoader.getInstance(context);
		this.progressBar = DictionaryLoadingBar.getInstance(context);
		this.onStart = onStart;
		this.onFinish = onFinish;
	}


	public void refreshStatus() {
		if (loader.isRunning()) {
			setBusy();
		} else {
			setReady();
		}
	}


	private void onLoadingStatusChange(Bundle status) {
		progressBar.show(status);
		item.setSummary(progressBar.getTitle() + " " + progressBar.getMessage());

		if (progressBar.isCancelled()) {
			setReady();
			onFinish.run();
		} else if (progressBar.isFailed()) {
			setReady();
			onFinish.run();
			UI.toastFromAsync(activity, progressBar.getMessage());
		} else if (!progressBar.inProgress()) {
			setReady();
			onFinish.run();
			UI.toastFromAsync(activity, R.string.dictionary_loaded);
		}
	}


	@Override
	protected boolean onClick(Preference p) {
		ArrayList<Language> languages = LanguageCollection.getAll(activity.getSettings().getEnabledLanguageIds());

		setBusy();
		if (!loader.load(activity, languages)) {
			loader.stop();
			setReady();
			onFinish.run();
		}

		return true;
	}


	private void setBusy() {
		loader.setOnStatusChange(this::onLoadingStatusChange);
		onStart.run();
		item.setTitle(activity.getString(R.string.dictionary_cancel_load));
	}


	private void setReady() {
		item.setTitle(activity.getString(R.string.dictionary_load_title));
		item.setSummary(progressBar.isFailed() || progressBar.isCancelled() ? progressBar.getMessage() : "");
	}
}
