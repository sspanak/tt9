package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportAlreadyRunningException;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.ui.DictionaryLoadingBar;
import io.github.sspanak.tt9.ui.UI;


public class ItemLoadDictionary extends ItemClickable {
	public static final String NAME = "dictionary_load";

	private final Context context;
	private final SettingsStore settings;
	private final DictionaryLoader loader;
	private final DictionaryLoadingBar progressBar;


	public ItemLoadDictionary(Preference item, Context context, SettingsStore settings, DictionaryLoader loader, DictionaryLoadingBar progressBar) {
		super(item);

		this.context = context;
		this.loader = loader;
		this.progressBar = progressBar;
		this.settings = settings;

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
		} else if (progressBar.isCompleted()) {
			setReadyStatus();
			UI.toastFromAsync(context, R.string.dictionary_loaded);
		}
	}


	@Override
	protected boolean onClick(Preference p) {
		ArrayList<Language> languages = LanguageCollection.getAll(context, settings.getEnabledLanguageIds());

		try {
			setLoadingStatus();
			loader.load(languages);
		} catch (DictionaryImportAlreadyRunningException e) {
			loader.stop();
			setReadyStatus();
		}

		return true;
	}


	private void setLoadingStatus() {
		disableOtherItems();
		item.setTitle(context.getString(R.string.dictionary_cancel_load));
	}


	private void setReadyStatus() {
		enableOtherItems();
		item.setTitle(context.getString(R.string.dictionary_load_title));
		item.setSummary(progressBar.isFailed() || progressBar.isCancelled() ? progressBar.getMessage() : "");
	}
}
