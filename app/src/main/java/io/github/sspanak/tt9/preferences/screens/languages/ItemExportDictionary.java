package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.db.exporter.DictionaryExporter;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

class ItemExportDictionary extends ItemExportAbstract {
	final public static String NAME = "dictionary_export";

	ItemExportDictionary(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item, activity, onStart, onFinish);
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
			.setLanguages(LanguageCollection.getAll(activity, activity.getSettings().getEnabledLanguageIds()))
			.export(activity);
	}

	public void setReadyStatus() {
		super.setReadyStatus();
		item.setSummary("");
	}
}
