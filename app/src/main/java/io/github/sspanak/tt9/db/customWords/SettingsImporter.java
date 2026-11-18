package io.github.sspanak.tt9.db.customWords;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.zip.InflaterOutputStream;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.settings.SettingsStatic;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.LimitedOutputStream;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SettingsImporter extends AbstractFileProcessor {
	private static final String LOG_TAG = SettingsImporter.class.toString();

	public static final String MIME_TYPE = "text/plain";
	public static final String FILE_EXTENSION = ".t9s";

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
			int total = 0;
			while ((n = in.read(chunk)) != -1) {
				total += n;
				if (total > SettingsStatic.SETTINGS_IMPORT_MAX_SIZE) {
					throw new IOException("Settings import file is too large.");
				}
				buffer.write(chunk, 0, n);
			}

			byte[] zipBytes = Base64.getDecoder().decode(buffer.toByteArray());
			buffer = new ByteArrayOutputStream();
			try (LimitedOutputStream ios = new LimitedOutputStream(new InflaterOutputStream(buffer), SettingsStatic.SETTINGS_IMPORT_MAX_SIZE * 4)) {
				ios.write(zipBytes);
			} catch (Exception exx) {
				Logger.e(LOG_TAG, "Failed to decompress settings import file. " + exx);
				throw new IOException("Failed to decompress settings import file. " + exx);
			}

			settings.importAll(buffer.toString());

			sendSuccess();
			Logger.d(LOG_TAG, "Settings import finished in: " + Timer.stop(LOG_TAG) + " ms");
		} catch (Exception ex) {
			Timer.stop(LOG_TAG);
			Logger.e(LOG_TAG, "Failed importing settings from JSON file. " + ex);
			sendFailure();
		}
	}


	public void run(@Nullable PreferencesActivity activity, @Nullable Uri uri) {
		fileUri = uri;
		settings = activity != null ? activity.getSettings() : null;
		if (activity != null) {
			super.run(activity);
		}
	}
}
