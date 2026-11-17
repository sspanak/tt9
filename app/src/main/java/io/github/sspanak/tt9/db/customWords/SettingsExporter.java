package io.github.sspanak.tt9.db.customWords;

import android.app.Activity;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.File;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.util.Logger;

public class SettingsExporter extends AbstractExporter {
	private static final String LOG_TAG = SettingsExporter.class.getSimpleName();
	private static final String BASE_FILE_NAME = "tt9-settings-";

	public SettingsExporter() {
		FILE_EXTENSION = SettingsImporter.FILE_EXTENSION;
		MIME_TYPE = SettingsImporter.MIME_TYPE;
	}

	public String getOutputFileName() {
		return getOutputFile()
			.replace(getOutputDir(), "")
			.replace(File.pathSeparator, "");
	}

	@NonNull
	@Override
	protected String generateFileName() {
		return BASE_FILE_NAME + System.currentTimeMillis() + FILE_EXTENSION;
	}

	@NonNull
	@Override
	protected byte[] getFileContents(Activity activity) throws Exception {
		byte[] bytes = new byte[0];

		try {
			if (activity instanceof PreferencesActivity) {
				bytes = ((PreferencesActivity) activity).getSettings().exportAll().getBytes();
			}
		} catch (JSONException ex) {
			Logger.e(LOG_TAG, "Failed exporting settings. " + ex);
			throw new Exception("Failed exporting settings. " + ex);
		}

		return bytes;
	}

	@Override
	protected void runSync(Activity activity) {
		try {
			sendStart("Exporting settings...");
			write(activity);
			sendSuccess();
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Failed exporting settings. " + e);
			sendFailure();
		}
	}
}
