package io.github.sspanak.tt9.preferences.screens.languages;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.preference.Preference;

import java.util.Locale;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.customWords.CustomWordsImporter;
import io.github.sspanak.tt9.db.entities.CustomWordFile;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemProcessCustomWordsAbstract;
import io.github.sspanak.tt9.ui.notifications.DictionaryProgressNotification;
import io.github.sspanak.tt9.util.Logger;

public class ItemImportCustomWords extends ItemProcessCustomWordsAbstract {
	final public static String NAME = "dictionary_import_custom";

	private ActivityResultLauncher<Intent> importCustomWordsLauncher;
	private String lastError;
	private float lastProgress;

	public ItemImportCustomWords(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item, activity, onStart, onFinish);
		getProcessor();
	}

	@Override
	protected CustomWordsImporter getProcessor() {
		return CustomWordsImporter.getInstance(activity);
	}

	@Override
	protected boolean onClick(Preference p) {
		setDefaultHandlers();
		getProcessor().setCancelHandler(this::onCancel);
		getProcessor().setFailureHandler(this::onFailure);
		getProcessor().setProgressHandler(this::onProgress);

		if (getProcessor().isRunning()) {
			getProcessor().cancel();
		} else {
			browseFiles();
		}
		return true;
	}

	@Override
	protected boolean onStartProcessing() {
		lastError = "";
		lastProgress = 0;
		return false;
	}

	private void onCancel() {
		activity.runOnUiThread(() -> {
			final String statusMsg = activity.getString(R.string.dictionary_import_canceled);

			setAndNotifyReady();
			DictionaryProgressNotification.getInstance(activity).showMessage("", statusMsg, statusMsg);

			item.setTitle(R.string.dictionary_import_custom_words);
			item.setSummary(statusMsg);
		});
	}

	private void onProgress(float progress) {
		final String numberFormat = (progress - lastProgress) < 2 ? "%1.3f%%" : "%1.0f%%";
		final String loadingMsg = activity.getString(R.string.dictionary_import_progress, String.format(Locale.getDefault(), numberFormat, progress));
		lastProgress = progress;

		DictionaryProgressNotification.getInstance(activity).showLoadingMessage(loadingMsg, "", Math.round(progress), 100);
		activity.runOnUiThread(() -> item.setSummary(loadingMsg));
	}

	private void onFailure(String error) {
		lastError = error;
		onFinishProcessing(null);
	}

	@Override
	protected String getFailureMessage() {
		return lastError;
	}

	@Override
	protected String getFailureTitle() {
		return activity.getString(R.string.dictionary_import_failed);
	}

	@Override
	protected String getSuccessMessage(String fileName) {
		return "";
	}

	@Override
	protected String getSuccessTitle() {
		return activity.getString(R.string.dictionary_import_finished);
	}

	@Override
	public void enable() {
		item.setSummary(R.string.dictionary_import_custom_words_summary);
		super.enable();
	}

	@Override
	public void disable() {
		if (getProcessor().isRunning()) {
			item.setTitle(R.string.dictionary_import_cancel);
		} else {
			super.disable();
		}
	}

	void setBrowseFilesLauncher(ActivityResultLauncher<Intent> launcher) {
		if (item != null) {
			item.setEnabled(true);
		}
		importCustomWordsLauncher = launcher;
	}

	private void browseFiles() {
		if (importCustomWordsLauncher == null) {
			Logger.w(getClass().getSimpleName(), "No file browser launcher set");
			return;
		}

		Intent intent = new Intent()
			.addCategory(Intent.CATEGORY_OPENABLE)
			.setType(CustomWordFile.MIME_TYPE)
			.setAction(Intent.ACTION_OPEN_DOCUMENT);

		importCustomWordsLauncher.launch(intent);
	}

	void onFileSelected(ActivityResult result) {
		if (result.getResultCode() != Activity.RESULT_OK) {
			onFailure(activity.getString(R.string.dictionary_import_error_browsing_error));
			Logger.e(getClass().getSimpleName(), "File picker activity failed with code: " + result.getResultCode());
			return;
		}

		CustomWordFile file = new CustomWordFile(
			result.getData() != null ? result.getData().getData() : null,
			activity.getContentResolver()
		);

		if (!file.exists()) {
			onFailure(activity.getString(R.string.dictionary_import_error_cannot_read_file));
			return;
		}

		getProcessor().run(activity, activity.getSettings(), file);
	}
}
