package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import io.github.sspanak.tt9.db.customWords.DictionaryExporter;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemExportAbstract;
import io.github.sspanak.tt9.preferences.items.ItemProcessCustomWordsAbstract;
import io.github.sspanak.tt9.util.Logger;

class ItemExportDictionary extends ItemExportAbstract {
	final public static String NAME = "dictionary_export";

	ItemExportDictionary(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item, activity, onStart, onFinish);
	}

	@Override
	public ItemProcessCustomWordsAbstract refreshStatus() {
		if (item != null) {
			item.setVisible(Logger.isDebugLevel());
		}
		return super.refreshStatus();
	}

	@Override
	protected DictionaryExporter getProcessor() {
		return DictionaryExporter.getInstance();
	}

	protected boolean onStartProcessing() {
		return DictionaryExporter.getInstance()
			.setLanguages(LanguageCollection.getAll(activity.getSettings().getEnabledLanguageIds()))
			.run(activity);
	}

	public void enable() {
		super.enable();
		item.setSummary("");
	}
}
