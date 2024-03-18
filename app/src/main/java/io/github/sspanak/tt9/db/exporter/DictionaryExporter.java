package io.github.sspanak.tt9.db.exporter;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.Timer;
import io.github.sspanak.tt9.db.sqlite.ReadOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.languages.Language;

public class DictionaryExporter extends AbstractExporter {
	private static DictionaryExporter self;

	public static final String LOG_TAG = "dictionary_export";
	private static final String BASE_FILE_NAME = "tt9-dictionary-export-";
	private ArrayList<Language> languages;
	private Language currentLanguage;

	public static DictionaryExporter getInstance() {
		if (self == null) {
			self = new DictionaryExporter();
		}

		return self;
	}

	@Override
	protected void exportSync(Activity activity) {
		if (languages == null || languages.isEmpty()) {
			Logger.d(LOG_TAG, "Nothing to do");
			return;
		}

		try {
			for (Language l : languages) {
				sendStart(activity.getString(R.string.dictionary_export_generating_csv_for_language, l.getName()));
				exportLanguage(activity, l);
			}
			sendSuccess();
		} catch (Exception e) {
			logExportError(e);
			sendFailure();
		}
	}

	public DictionaryExporter setLanguages(ArrayList<Language> languages) {
		this.languages = languages;
		return this;
	}

	@Override
	@NonNull
	protected String generateFileName() {
		return BASE_FILE_NAME + currentLanguage.getLocale().getLanguage() + "-" + System.currentTimeMillis() + FILE_EXTENSION;
	}

	@Override
	@NonNull
	protected byte[] getWords(Activity activity) throws Exception {
		SQLiteDatabase db = SQLiteOpener.getInstance(activity).getDb();
		if (db == null) {
			throw new Exception("Could not open database");
		}

		return new ReadOps().getWords(db, currentLanguage, false).getBytes();
	}

	private void exportLanguage(Activity activity, Language language) throws Exception {
		currentLanguage = language;
		if (currentLanguage == null) {
			Logger.e(LOG_TAG, "Cannot export dictionary for null language");
			return;
		}

		Timer.start(LOG_TAG);
		write(activity);
		Logger.d(LOG_TAG, "All words for language '" + currentLanguage.getName() + "' exported. Time: " + Timer.stop(LOG_TAG) + "ms");
	}

	private void logExportError(Exception e) {
		String message;
		if (currentLanguage == null) {
			message = "Failed exporting '" + getOutputFile() + "' . " + e;
		} else {
			message = "Failed exporting dictionary for '" + currentLanguage.getName() + "' to '" + getOutputFile() + "'. " + e;
		}
		Logger.e(LOG_TAG, message);
	}
}
