package io.github.sspanak.tt9.db.customWords;

import android.app.Activity;
import android.net.Uri;

import androidx.annotation.Nullable;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;

public class SettingsImporter extends AbstractFileProcessor {
	private static final String LOG_TAG = SettingsImporter.class.toString();

	public static final String MIME_TYPE = "application/json";
	public static final String FILE_EXTENSION = ".json";

	@Nullable private Uri fileUri = null;
	@Nullable private SettingsStore settings = null;


	@Override
	protected void sendSuccess() {
		if (successHandler != null) {
			successHandler.accept(null);
		}
	}


	@Override
	protected void runSync(Activity activity) {
		if (fileUri == null || settings == null) {
			return;
		}

		Timer.start(LOG_TAG);

		try (InputStream in = activity.getContentResolver().openInputStream(fileUri)) {
			if (in == null) {
				sendFailure();
				return;
			}

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] chunk = new byte[4096];
			int n;
			while ((n = in.read(chunk)) != -1) buffer.write(chunk, 0, n);

			settings.importAll(buffer.toString());

			sendSuccess();
			Logger.d(LOG_TAG, "Settings import finished in: " + Timer.stop(LOG_TAG) + " ms");
		} catch (IOException | JSONException ex) {
			Timer.stop(LOG_TAG);
			Logger.e(LOG_TAG, "Failed importing settings from JSON file. " + ex);
			sendFailure();
		}
	}


	public void run(@Nullable PreferencesActivity activity, @Nullable Uri uri) {
		fileUri = uri;
		settings = activity != null ? activity.getSettings() : null;
		runSync(activity);
	}
}
