package io.github.sspanak.tt9.db.customWords;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Consumer;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.db.entities.CustomWordFile;
import io.github.sspanak.tt9.db.sqlite.InsertOps;
import io.github.sspanak.tt9.db.sqlite.ReadOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;

public class CustomWordsImporter extends AbstractFileProcessor {
	private static CustomWordsImporter self;

	private Consumer<Integer> progressHandler;
	private Consumer<String> failureHandler;

	private CustomWordFile file;
	private final Resources resources;
	private SQLiteOpener sqlite;

	private long lastProgressUpdate = 0;


	public static CustomWordsImporter getInstance(Context context) {
		if (self == null) {
			self = new CustomWordsImporter(context);
		}

		return self;
	}


	private CustomWordsImporter(Context context) {
		super();
		this.resources = context.getResources();
	}


	public void clearAllHandlers() {
		failureHandler = null;
		progressHandler = null;
		successHandler = null;
	}


	public void setProgressHandler(Consumer<Integer> handler) {
		progressHandler = handler;
	}


	public void setFailureHandler(Consumer<String> handler) {
		failureHandler = handler;
	}


	private void sendProgress(int progress) {
		long now = System.currentTimeMillis();
		if (lastProgressUpdate + SettingsStore.DICTIONARY_IMPORT_PROGRESS_UPDATE_TIME < now) {
			progressHandler.accept(progress);
			lastProgressUpdate = now;
		}
	}


	@Override
	protected void sendSuccess() {
		if (successHandler != null) {
			successHandler.accept(file.getName());
			clearAllHandlers();
		}
	}


	private void sendFailure(String errorMessage) {
		if (failureHandler != null) {
			failureHandler.accept(errorMessage);
			clearAllHandlers();
		}
	}


	public void run(@NonNull Activity activity, @NonNull CustomWordFile file) {
		this.file = file;
		super.run(activity);
	}


	@Override
	protected void runSync(Activity activity) {
		Timer.start(getClass().getSimpleName());

		sendStart(resources.getString(R.string.dictionary_import_running));
		if (isFileValid() && isThereRoomForMoreWords(activity) && insertWords()) {
			sendSuccess();
			Logger.i(getClass().getSimpleName(), "Imported " + file.getName() + " in " + Timer.get(getClass().getSimpleName()) + " ms");
		} else {
			Logger.e(getClass().getSimpleName(), "Failed to import " + file.getName());
		}
	}


	private boolean openDb(Context context) {
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

				if (customWord.language.isTranscribed() || readOps.exists(sqlite.getDb(), customWord.language, customWord.word)) {
					ignoredWords++;
				} else {
					InsertOps.insertCustomWord(sqlite.getDb(), customWord.language, customWord.sequence, customWord.word);
				}

				if (file.getSize() > 20) {
					sendProgress(lineCount * 100 / file.getSize());
				}
			}

			sqlite.finishTransaction();
		} catch (IOException e) {
			sqlite.failTransaction();
			Logger.e(getClass().getSimpleName(), "Error opening the file. " + e.getMessage());
			sendFailure(resources.getString(R.string.dictionary_import_error_cannot_read_file));
			return false;
		}

		if (ignoredWords > 0) {
			Logger.i(
				getClass().getSimpleName(),
				"Skipped " + ignoredWords + " word(s) that are already in the dictionary or do not belong to an alphabetic language."
			);
		}

		return true;
	}


	private boolean isFileValid() {
		if (file != null) {
			return true;
		}

		Logger.e(getClass().getSimpleName(), "Can not read a NULL file");
		sendFailure(resources.getString(R.string.dictionary_import_error_cannot_read_file));
		return false;
	}


	private boolean isThereRoomForMoreWords(Context context) {
		if (!openDb(context)) {
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
				CustomWordFile.getLanguage(line)
			);
		} catch (Exception e) {
			String linePreview = line.length() > 50 ? line.substring(0, 50) + "..." : line;
			sendFailure(resources.getString(R.string.dictionary_import_error_malformed_line, linePreview, lineCount));
			return null;
		}
	}
}
