package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.exporter.CustomWordsExporter;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

class ItemExportCustomWords extends ItemExportAbstract {
	final public static String NAME = "dictionary_export_custom";

	ItemExportCustomWords(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item, activity, onStart, onFinish);
	}

	@Override
	protected CustomWordsExporter getExporter() {
		return CustomWordsExporter.getInstance();
	}

	protected boolean onStartExporting() {
		return CustomWordsExporter.getInstance().export(activity);
	}

	public void setReadyStatus() {
		super.setReadyStatus();
		item.setSummary(activity.getString(
			R.string.dictionary_export_custom_words_summary,
			CustomWordsExporter.getInstance().getOutputDir()
		));
	}
}
