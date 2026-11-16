package io.github.sspanak.tt9.preferences.screens.backup;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.customWords.SettingsImporter;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.util.Logger;

public class PreferenceImportSettings extends ScreenPreference {
	public static final String NAME = "import_settings";

	private PreferencesActivity activity;
	private SettingsImporter importer;
	private ActivityResultLauncher<Intent> importSettingsLauncher;

	public PreferenceImportSettings(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(context); }
	public PreferenceImportSettings(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }
	public PreferenceImportSettings(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(context); }
	public PreferenceImportSettings(@NonNull Context context) { super(context); init(context); }


	private void init(@NonNull Context context) {
		setKey(NAME);
		setTitle(R.string.backup_import);
		setSummary(context.getString(R.string.backup_import_summary));

		importer = new SettingsImporter();
		importer.setFailureHandler(this::onFailure);
		importer.setSuccessHandler(this::onSuccess);
	}


	@Override
	public int getLargeLayout() {
		return R.layout.pref_default_large;
	}


	@Override
	protected void onClick() {
		if (activity == null) {
			return;
		}

		super.onClick();
		if (importSettingsLauncher == null) {
			Logger.w(getClass().getSimpleName(), "No file browser launcher set");
			return;
		}

		// Prevent factory words autoload when the browse file activity pops up
		DictionaryLoader.setSkipNextAutoLoad();

		Intent intent = new Intent()
			.addCategory(Intent.CATEGORY_OPENABLE)
			.setType(SettingsImporter.MIME_TYPE)
			.setAction(Intent.ACTION_OPEN_DOCUMENT);

		importSettingsLauncher.launch(intent);
	}


	public PreferenceImportSettings populate(@NonNull PreferencesActivity activity) {
		this.activity = activity;
		setEnabled(!activity.getSettings().getDemoMode());
		return this;
	}


	public void onFileSelected(ActivityResult result) {
		importer.run(activity, result.getData() != null ? result.getData().getData() : null);
	}


	public void setBrowseFilesLauncher(ActivityResultLauncher<Intent> launcher) {
		this.importSettingsLauncher = launcher;
	}


	private void onFailure() {
		activity.runOnUiThread(() -> setSummary(R.string.backup_import_failed));
	}


	private void onSuccess(String s) {
		activity.runOnUiThread(() -> setSummary(R.string.backup_import_success));
	}
}
