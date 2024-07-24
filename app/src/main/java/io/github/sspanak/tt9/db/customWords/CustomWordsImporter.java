package io.github.sspanak.tt9.db.customWords;

import android.app.Activity;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;

import io.github.sspanak.tt9.db.entities.CustomWordsFile;
import io.github.sspanak.tt9.db.entities.WordFile;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

public class CustomWordsImporter extends AbstractFileProcessor {
	private ConsumerCompat<String> failureHandler;

	private CustomWordsFile file;
	private final Resources resources;

	public CustomWordsImporter(Resources resources) {
		super();
		this.resources = resources;
	}

	public void setFailureHandler(ConsumerCompat<String> handler) {
		failureHandler = handler;
	}


	@Override
	protected void sendSuccess() {

	}


	private void sendFailure(int errorMessageId) {
		if (failureHandler != null) {
			failureHandler.accept(resources.getString(errorMessageId));
		}
	}


	private void sendFailure(String message, int userMessageId) {
		Logger.e(getClass().getSimpleName(), "Importing failed. " + message);
		sendFailure(userMessageId);
	}


	@Override
	protected void runSync(Activity activity) {
		if (file == null) {
			// @todo: send a proper user message
			sendFailure("Can not read a NULL file", 0);
			return;
		}

		// @todo: start a transaction

		int lineCount = 1;
		try (BufferedReader reader = file.getReader()) {
			for (String line; (line = reader.readLine()) != null; lineCount++) {
				if (lineCount > SettingsStore.CUSTOM_WORDS_IMPORT_MAX_LINES) {
					sendFailure("File too long.", 0);
					// @todo: send a proper user message
					return;
				}

				if (!CustomWordsFile.isLineValid(line)) {
					String linePreview = line.length() > 50 ? line.substring(0, 50) + "..." : line;
					sendFailure("Line " + lineCount + " is invalid. Unexpected format: '" + linePreview + "'", 0);
					// @todo: send a proper user message
					return;
				}

				String[] parts = WordFile.splitLine(line);

				// @todo: add to database
				Logger.d("ImportLoop", "====> Importing word: " + parts[0] + " language ID: " + parts[1]);
			}
		} catch (IOException e) {
			// @todo: abort transaction
			// @todo: send a proper user message
			sendFailure("Error opening the file. " + e.getMessage(), 0);
		}
	}


	public boolean run(@NonNull Activity activity, @NonNull CustomWordsFile file) {
		this.file = file;
		return super.run(activity);
	}
}
