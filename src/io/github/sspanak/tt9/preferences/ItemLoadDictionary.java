package io.github.sspanak.tt9.preferences;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryImportAlreadyRunningException;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.ui.DictionaryLoadingBar;
import io.github.sspanak.tt9.ui.UI;


public class ItemLoadDictionary extends ItemClickable {
	public static final String NAME = "dictionary_load";

	private final Context context;
	private final SettingsStore settings;
	private final DictionaryLoader loader;
	private final DictionaryLoadingBar progressBar;


	ItemLoadDictionary(Preference item, Context context, SettingsStore settings, DictionaryLoader loader, DictionaryLoadingBar progressBar) {
		super(item);

		this.context = context;
		this.loader = loader;
		this.progressBar = progressBar;
		this.settings = settings;

		loader.setStatusHandler(onDictionaryLoading);

		if (!progressBar.isCompleted() && !progressBar.isFailed()) {
			changeToCancelButton();
		} else {
			changeToLoadButton();
		}
	}


	private final Handler onDictionaryLoading = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			progressBar.show(msg.getData());
			item.setSummary(progressBar.getTitle() + " " + progressBar.getMessage());

			if (progressBar.isCompleted()) {
				changeToLoadButton();
				UI.toast(context, R.string.dictionary_loaded);
			} else if (progressBar.isFailed()) {
				changeToLoadButton();
				UI.toast(context, R.string.dictionary_load_failed);
			}
		}
	};


	@Override
	protected boolean onClick(Preference p) {
		ArrayList<Language> languages = LanguageCollection.getAll(settings.getEnabledLanguageIds());
		progressBar.setFileCount(languages.size());

		try {
			loader.load(languages);
			changeToCancelButton();
		} catch (DictionaryImportAlreadyRunningException e) {
			loader.stop();
			changeToLoadButton();
		}

		return false;
	}


	public void changeToCancelButton() {
		item.setTitle(context.getString(R.string.dictionary_cancel_load));
	}


	public void changeToLoadButton() {
		item.setTitle(context.getString(R.string.dictionary_load_title));
		item.setSummary(progressBar.isFailed() ? progressBar.getMessage() : "");
	}
}
