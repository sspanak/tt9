package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.exporter.AbstractExporter;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.ui.DictionaryNotification;

abstract class ItemExportAbstract extends ItemClickable {
	final protected PreferencesActivity activity;
	final private Runnable onStart;
	final private Runnable onFinish;

	ItemExportAbstract(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item);
		this.activity = activity;
		this.onStart = onStart;
		this.onFinish = onFinish;

		AbstractExporter exporter = getExporter();
		exporter.setFailureHandler(() -> onFinishExporting(null));
		exporter.setStartHandler(() -> activity.runOnUiThread(this::setLoadingStatus));
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

		String loadingMessage = getExporter().getStatusMessage();
		item.setSummary(loadingMessage);
		DictionaryNotification.getInstance(activity).showLoadingMessage(loadingMessage, "");
	}


	public void setReadyStatus() {
		enable();
		onFinish.run();
	}
}
