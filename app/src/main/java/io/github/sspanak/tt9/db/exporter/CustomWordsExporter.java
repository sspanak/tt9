package io.github.sspanak.tt9.db.exporter;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.db.sqlite.ReadOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;

public class CustomWordsExporter extends AbstractExporter {
	private static CustomWordsExporter customWordsExporterSelf;

	public static final String LOG_TAG = "dictionary_export";
	private static final String BASE_FILE_NAME = "tt9-added-words-export-";

	public static CustomWordsExporter getInstance() {
		if (customWordsExporterSelf == null) {
			customWordsExporterSelf = new CustomWordsExporter();
		}

		return customWordsExporterSelf;
	}

	public boolean export(Activity activity) {
		if (isRunning()) {
			return false;
		}

		processThread = new Thread(() -> {
			try {
				write(activity);
				sendSuccess();
			} catch (Exception e) {
				sendFailure();
			}
		});

		processThread.start();
		return true;
	}

	@Override
	@NonNull
	protected String generateFileName() {
		return BASE_FILE_NAME + "-" + System.currentTimeMillis() + FILE_EXTENSION;
	}

	@NonNull
	@Override
	protected byte[] getWords(Activity activity) throws Exception {
		SQLiteDatabase db = SQLiteOpener.getInstance(activity).getDb();
		if (db == null) {
			throw new Exception("Could not open database");
		}

		return new ReadOps().getWords(db, null, true).getBytes();
	}
}
