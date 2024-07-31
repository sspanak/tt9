package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.customWords.CustomWordsExporter;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemExportAbstract;

class ItemExportCustomWords extends ItemExportAbstract {
	final public static String NAME = "dictionary_export_custom";

	ItemExportCustomWords(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item, activity, onStart, onFinish);
	}

	@Override
	protected CustomWordsExporter getProcessor() {
		return CustomWordsExporter.getInstance();
	}

	protected boolean onStartProcessing() {
		return CustomWordsExporter.getInstance().run(activity);
	}

	public void setReadyStatus() {
		super.setReadyStatus();
		item.setSummary(activity.getString(
			R.string.dictionary_export_custom_words_summary,
			CustomWordsExporter.getInstance().getOutputDir()
		));
	}
}
