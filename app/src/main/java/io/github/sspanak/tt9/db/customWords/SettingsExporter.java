package io.github.sspanak.tt9.db.customWords;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.util.Logger;

@RequiresApi(api = Build.VERSION_CODES.O)
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
				final byte[] json = ((PreferencesActivity) activity).getSettings().exportAll().getBytes();
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				try (DeflaterOutputStream dfStream = new DeflaterOutputStream(byteStream)) {
					dfStream.write(json);
				} catch (Exception exx) {
					Logger.e(LOG_TAG, "Failed to compress settings export file. " + exx);
					throw new IOException("Failed to compress settings export file. " + exx);
				}

				bytes = new String(Base64.getEncoder().encode(byteStream.toByteArray())).getBytes();
			}
		} catch (JSONException ex) {
			Logger.e(LOG_TAG, "Failed exporting settings. " + ex);
			throw new IOException("Failed exporting settings. " + ex);
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
