package io.github.sspanak.tt9.db.customWords;

import android.app.Activity;

import androidx.annotation.NonNull;

public class SettingsExporter extends AbstractExporter {
	public SettingsExporter() {
		FILE_EXTENSION = ".json";
		MIME_TYPE = "application/json";
	}

	@NonNull
	@Override
	protected String generateFileName() {
		return "";
	}

	@NonNull
	@Override
	protected byte[] getFileContents(Activity activity) throws Exception {
		return new byte[0];
	}

	@Override
	protected void runSync(Activity activity) {

	}
}
