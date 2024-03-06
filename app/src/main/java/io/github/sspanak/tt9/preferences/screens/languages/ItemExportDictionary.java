package io.github.sspanak.tt9.preferences.screens.languages;

import android.app.Activity;

import androidx.preference.Preference;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.exporter.DictionaryExporter;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;

class ItemExportDictionary extends ItemExportAbstract {
	final public static String NAME = "dictionary_export";

	protected final SettingsStore settings;


	ItemExportDictionary(Preference item, Activity activity, SettingsStore settings, Runnable onStart, Runnable onFinish) {
		super(item, activity, onStart, onFinish);
		this.settings = settings;
	}


	@Override
	public ItemExportAbstract refreshStatus() {
		if (item != null) {
			item.setVisible(Logger.isDebugLevel());
		}
		return super.refreshStatus();
	}


	@Override
	protected DictionaryExporter getExporter() {
		return DictionaryExporter.getInstance();
	}


	protected boolean onStartExporting() {
		return DictionaryExporter.getInstance()
			.setLanguages(LanguageCollection.getAll(activity, settings.getEnabledLanguageIds()))
			.export(activity);
	}


	protected void setLoadingStatus() {
		super.setLoadingStatus();
		item.setSummary(R.string.dictionary_export_running);
	}


	public void setReadyStatus() {
		super.setReadyStatus();
		item.setSummary("");
	}
}
