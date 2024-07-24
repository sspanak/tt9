package io.github.sspanak.tt9.db.customWords;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.entities.CustomWordFile;
import io.github.sspanak.tt9.db.entities.WordFile;
import io.github.sspanak.tt9.db.sqlite.ReadOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

public class CustomWordsImporter extends AbstractFileProcessor {
	private ConsumerCompat<String> failureHandler;

	private CustomWordFile file;
	private final Context context;
	private final Resources resources;

	public CustomWordsImporter(Context context) {
		super();
		this.context = context;
		this.resources = context.getResources();
	}

	public void setFailureHandler(ConsumerCompat<String> handler) {
		failureHandler = handler;
	}


	@Override
	protected void sendSuccess() {

	}


	private void sendFailure(String errorMessage) {
		if (failureHandler != null) {
			failureHandler.accept(errorMessage);
		}
	}


	@Override
	protected void runSync(Activity activity) {
		if (file == null) {
			Logger.e(getClass().getSimpleName(), "Can not read a NULL file");
			sendFailure(resources.getString(R.string.dictionary_import_error_cannot_read_file));
			return;
		}

		if (areCustomWordsTooMany()) {
			return;
		}

		// @todo: start a transaction

		int lineCount = 1;
		try (BufferedReader reader = file.getReader()) {
			for (String line; (line = reader.readLine()) != null; lineCount++) {
				if (!isLineCountValid(lineCount) || !isLineValid(line, lineCount)) {
					return;
				}

				String[] parts = WordFile.splitLine(line);

				// @todo: add to database
				Logger.d("ImportLoop", "====> Importing word: " + parts[0] + " language ID: " + parts[1]);
			}
		} catch (IOException e) {
			// @todo: abort transaction
			Logger.e(getClass().getSimpleName(), "Error opening the file. " + e.getMessage());
			sendFailure(resources.getString(R.string.dictionary_import_error_cannot_read_file));
		}
	}


	public boolean run(@NonNull Activity activity, @NonNull CustomWordFile file) {
		this.file = file;
		return super.run(activity);
	}


	private boolean areCustomWordsTooMany() {
		SQLiteDatabase db = SQLiteOpener.getInstance(context).getDb();
		if (db == null) {
			Logger.e(getClass().getSimpleName(), "Could not open database");
			sendFailure(resources.getString(R.string.dictionary_import_failed));
			return true;
		}

		if ((new ReadOps()).countCustomWords(db) > SettingsStore.CUSTOM_WORDS_MAX) {
			sendFailure(resources.getString(R.string.dictionary_import_error_too_many_words));
			return true;
		}

		return false;
	}


	private boolean isLineCountValid(int lineCount) {
		if (lineCount <= SettingsStore.CUSTOM_WORDS_IMPORT_MAX_LINES) {
			return true;
		}

		sendFailure(resources.getString(R.string.dictionary_import_error_file_too_long, SettingsStore.CUSTOM_WORDS_IMPORT_MAX_LINES));
		return false;
	}


	private boolean isLineValid(String line, int lineCount) {
		if (CustomWordFile.isLineValid(line)) {
			return true;
		}

		String linePreview = line.length() > 50 ? line.substring(0, 50) + "..." : line;
		sendFailure(resources.getString(R.string.dictionary_import_error_malformed_line, linePreview, lineCount));
		return false;
	}
}
