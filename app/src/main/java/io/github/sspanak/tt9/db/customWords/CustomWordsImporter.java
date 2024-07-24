package io.github.sspanak.tt9.db.customWords;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.db.entities.CustomWordFile;
import io.github.sspanak.tt9.db.sqlite.InsertOps;
import io.github.sspanak.tt9.db.sqlite.ReadOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

public class CustomWordsImporter extends AbstractFileProcessor {
	private ConsumerCompat<String> failureHandler;

	private final Context context;
	private CustomWordFile file;
	private final Resources resources;
	SQLiteOpener sqlite;


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
		if (successHandler != null) {
			successHandler.accept(file.getName());
		}
	}


	private void sendFailure(String errorMessage) {
		if (failureHandler != null) {
			failureHandler.accept(errorMessage);
		}
	}


	public boolean run(@NonNull Activity activity, @NonNull CustomWordFile file) {
		this.file = file;
		return super.run(activity);
	}


	@Override
	protected void runSync(Activity activity) {
		sendStart(resources.getString(R.string.dictionary_import_running));
		if (isFileValid() && isThereRoomForMoreWords() && insertWords()) {
			sendSuccess();
		}
	}


	private boolean openDb() {
		sqlite = SQLiteOpener.getInstance(context);
		if (sqlite.getDb() != null) {
			return true;
		}

		Logger.e(getClass().getSimpleName(), "Could not open database");
		sendFailure(resources.getString(R.string.dictionary_import_failed));
		return false;
	}


	private boolean insertWords() {
		ReadOps readOps = new ReadOps();
		int ignoredWords = 0;
		int lineCount = 1;


		try (BufferedReader reader = file.getReader()) {
			sqlite.beginTransaction();

			for (String line; (line = reader.readLine()) != null; lineCount++) {
				if (!isLineCountValid(lineCount)) {
					sqlite.failTransaction();
					return false;
				}

				CustomWord customWord = createCustomWord(line, lineCount);
				if (customWord == null) {
					sqlite.failTransaction();
					return false;
				}

				if (readOps.exists(sqlite.getDb(), customWord.language, customWord.word)) {
					ignoredWords++;
					continue;
				}

				InsertOps.insertCustomWord(sqlite.getDb(), customWord.language, customWord.sequence, customWord.word);
			}

			sqlite.finishTransaction();
			return true;
		} catch (IOException e) {
			sqlite.failTransaction();
			Logger.e(getClass().getSimpleName(), "Error opening the file. " + e.getMessage());
			sendFailure(resources.getString(R.string.dictionary_import_error_cannot_read_file));
			return false;
		}
	}


	private boolean isFileValid() {
		if (file != null) {
			return true;
		}

		Logger.e(getClass().getSimpleName(), "Can not read a NULL file");
		sendFailure(resources.getString(R.string.dictionary_import_error_cannot_read_file));
		return false;
	}


	private boolean isThereRoomForMoreWords() {
		if (!openDb()) {
			return false;
		}

		if ((new ReadOps()).countCustomWords(sqlite.getDb()) > SettingsStore.CUSTOM_WORDS_MAX) {
			sendFailure(resources.getString(R.string.dictionary_import_error_too_many_words));
			return false;
		}

		return true;
	}


	private boolean isLineCountValid(int lineCount) {
		if (lineCount <= SettingsStore.CUSTOM_WORDS_IMPORT_MAX_LINES) {
			return true;
		}

		sendFailure(resources.getString(R.string.dictionary_import_error_file_too_long, SettingsStore.CUSTOM_WORDS_IMPORT_MAX_LINES));
		return false;
	}


	private CustomWord createCustomWord(String line, int lineCount) {
		try {
			return new CustomWord(
				CustomWordFile.getWord(line),
				CustomWordFile.getLanguage(context, line)
			);
		} catch (Exception e) {
			String linePreview = line.length() > 50 ? line.substring(0, 50) + "..." : line;
			sendFailure(resources.getString(R.string.dictionary_import_error_malformed_line, linePreview, lineCount));
			return null;
		}
	}
}
