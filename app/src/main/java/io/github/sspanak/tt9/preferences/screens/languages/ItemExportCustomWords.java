package io.github.sspanak.tt9.preferences.screens.languages;

import android.app.Activity;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.exporter.CustomWordsExporter;

class ItemExportCustomWords extends ItemExportAbstract {
	final public static String NAME = "dictionary_export_custom";


	ItemExportCustomWords(Preference item, Activity activity, Runnable onStart, Runnable onFinish) {
		super(item, activity, onStart, onFinish);
	}


	@Override
	protected CustomWordsExporter getExporter() {
		return CustomWordsExporter.getInstance();
	}


	protected boolean onStartExporting() {
		return CustomWordsExporter.getInstance().export(activity);
	}


	@Override
	protected String getLoadingMessage() {
		return activity.getString(R.string.dictionary_export_generating_csv);
	}

	public void setReadyStatus() {
		super.setReadyStatus();
		item.setSummary(activity.getString(
			R.string.dictionary_export_custom_words_summary,
			CustomWordsExporter.getInstance().getOutputDir()
		));
	}
}
