package io.github.sspanak.tt9.preferences.screens.languages;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.customWords.CustomWordsImporter;
import io.github.sspanak.tt9.db.entities.CustomWordFile;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemProcessCustomWordsAbstract;
import io.github.sspanak.tt9.util.Logger;

public class ItemImportCustomWords extends ItemProcessCustomWordsAbstract {
	final public static String NAME = "dictionary_import_custom";

	private ActivityResultLauncher<Intent> importCustomWordsLauncher;
	private CustomWordsImporter importer;

	public ItemImportCustomWords(Preference item, PreferencesActivity activity, Runnable onStart, Runnable onFinish) {
		super(item, activity, onStart, onFinish);

		// @todo: display the message to the user
		getProcessor().setFailureHandler((error) -> Logger.e("UNIMPLEMENTEDERRORHANDLER", "Import failed: " + error));
	}

	@Override
	protected CustomWordsImporter getProcessor() {
		if (importer == null) {
			importer = new CustomWordsImporter(activity);
		}
		return importer;
	}

	@Override
	protected boolean onClick(Preference p) {
		browseFiles();
		return true;
	}

	@Override
	protected boolean onStartProcessing() {
		return false;
	}

	@Override
	protected String getFailureMessage() {
		return "";
	}

	@Override
	protected String getFailureTitle() {
		return "";
	}

	@Override
	protected String getSuccessMessage(String fileName) {
		return "";
	}

	@Override
	protected String getSuccessTitle() {
		return "";
	}

	@Override
	public void setReadyStatus() {
		item.setSummary(R.string.dictionary_import_custom_words_summary);
		super.setReadyStatus();
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
			.setType(CustomWordFile.MIME_TYPE) // text/csv does not work for some reason
			.setAction(Intent.ACTION_GET_CONTENT);

		importCustomWordsLauncher.launch(intent);
	}

	void onFileSelected(ActivityResult result) {
		if (result.getResultCode() != Activity.RESULT_OK) {
			Logger.e(getClass().getSimpleName(), "File picker activity failed with code: " + result.getResultCode());
			return;
		}

		CustomWordFile file = new CustomWordFile(
			result.getData() != null ? result.getData().getData() : null,
			activity.getContentResolver()
		);

		if (!file.exists()) {
			Logger.e(getClass().getSimpleName(), "Failed opening file or file does not exist.");
			// @todo: show error message
			return;
		}

		getProcessor().run(activity, file);
	}
}
