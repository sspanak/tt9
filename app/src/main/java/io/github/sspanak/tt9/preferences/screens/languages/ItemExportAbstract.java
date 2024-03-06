package io.github.sspanak.tt9.preferences.screens.languages;

import android.app.Activity;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.exporter.AbstractExporter;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.ui.DictionaryNotification;

abstract class ItemExportAbstract extends ItemClickable {
	final protected Activity activity;
	final private Runnable onStart;
	final private Runnable onFinish;

	ItemExportAbstract(Preference item, Activity activity, Runnable onStart, Runnable onFinish) {
		super(item);
		this.activity = activity;
		this.onStart = onStart;
		this.onFinish = onFinish;

		AbstractExporter exporter = getExporter();
		exporter.setFailureHandler(() -> onFinishExporting(null));
		exporter.setSuccessHandler(this::onFinishExporting);
		refreshStatus();
	}

	abstract protected AbstractExporter getExporter();


	public ItemExportAbstract refreshStatus() {
		if (item != null) {
			if (getExporter().isRunning()) {
				setLoadingStatus();
			} else {
				setReadyStatus();
			}
		}
		return this;
	}


	@Override
	protected boolean onClick(Preference p) {
		setLoadingStatus();
		if (!onStartExporting()) {
			setReadyStatus();
		}
		return true;
	}


	abstract protected boolean onStartExporting();


	protected void onFinishExporting(String outputFile) {
		activity.runOnUiThread(() -> {
			setReadyStatus();

			if (outputFile == null) {
				DictionaryNotification.getInstance(activity).showError(
					activity.getString(R.string.dictionary_export_failed),
					activity.getString(R.string.dictionary_export_failed_more_info)
				);
			} else {
				DictionaryNotification.getInstance(activity).showMessage(
					activity.getString(R.string.dictionary_export_finished),
					activity.getString(R.string.dictionary_export_finished_more_info, outputFile),
					activity.getString(R.string.dictionary_export_finished_more_info, outputFile)
				);
			}
		});
	}


	protected void setLoadingStatus() {
		onStart.run();
		disable();
		item.setSummary(R.string.dictionary_export_running);
		DictionaryNotification.getInstance(activity).showLoadingMessage(
			activity.getString(R.string.dictionary_export_running),
			""
		);
	}


	public void setReadyStatus() {
		enable();
		onFinish.run();
	}
}
