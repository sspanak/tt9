package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.ui.DictionaryLoadingBar;
import io.github.sspanak.tt9.ui.UI;


public class ItemLoadDictionary extends ItemClickable {
	public final static String NAME = "dictionary_load";

	private final Context context;
	private final SettingsStore settings;
	private final Runnable onStart;
	private final Runnable onFinish;

	private final DictionaryLoader loader;
	private final DictionaryLoadingBar progressBar;


	public ItemLoadDictionary(Preference item, Context context, SettingsStore settings, Runnable onStart, Runnable onFinish) {
		super(item);

		this.context = context;
		this.loader = DictionaryLoader.getInstance(context);
		this.progressBar = DictionaryLoadingBar.getInstance(context);
		this.settings = settings;
		this.onStart = onStart;
		this.onFinish = onFinish;


		loader.setOnStatusChange(this::onLoadingStatusChange);
		refreshStatus();
	}


	public void refreshStatus() {
		if (loader.isRunning()) {
			setLoadingStatus();
		} else {
			setReadyStatus();
		}
	}


	private void onLoadingStatusChange(Bundle status) {
		progressBar.show(context, status);
		item.setSummary(progressBar.getTitle() + " " + progressBar.getMessage());

		if (progressBar.isCancelled()) {
			setReadyStatus();
		} else if (progressBar.isFailed()) {
			setReadyStatus();
			UI.toastFromAsync(context, progressBar.getMessage());
		} else if (!progressBar.inProgress()) {
			setReadyStatus();
			UI.toastFromAsync(context, R.string.dictionary_loaded);
		}
	}


	@Override
	protected boolean onClick(Preference p) {
		ArrayList<Language> languages = LanguageCollection.getAll(context, settings.getEnabledLanguageIds());

		setLoadingStatus();
		if (!loader.load(languages)) {
			loader.stop();
			setReadyStatus();
		}

		return true;
	}


	private void setLoadingStatus() {
		onStart.run();
		item.setTitle(context.getString(R.string.dictionary_cancel_load));
	}


	private void setReadyStatus() {
		onFinish.run();
		item.setTitle(context.getString(R.string.dictionary_load_title));
		item.setSummary(progressBar.isFailed() || progressBar.isCancelled() ? progressBar.getMessage() : "");
	}
}
