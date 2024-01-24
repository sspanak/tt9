package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;


public class UpdateOperations {
	private static final String LOG_TAG = UpdateOperations.class.getSimpleName();
	private static SQLiteStatement affectedRowsStatement = null;
	private static final HashMap<Integer, SQLiteStatement> changeFreqStatements = new HashMap<>();
	private static final HashMap<Integer, SQLiteStatement> normalizeStatements = new HashMap<>();
	private static SQLiteStatement scheduleNormalizationStatement = null;


	private static SQLiteStatement getAffectedRowsStatement(@NonNull SQLiteDatabase db) {
		return affectedRowsStatement != null ? affectedRowsStatement : (affectedRowsStatement = db.compileStatement("SELECT changes()"));
	}


	private static SQLiteStatement getChangeFreqStatement(@NonNull SQLiteDatabase db, @NonNull Language language) {
		if (!changeFreqStatements.containsKey(language.getId())) {
			String updateSQL = "UPDATE " + TableOperations.getWordsTable(language.getId()) + " SET frequency = ? WHERE position = ?";
			changeFreqStatements.put(language.getId(), db.compileStatement(updateSQL));
		}

		return changeFreqStatements.get(language.getId());
	}


	private static SQLiteStatement getNormalizeStatement(@NonNull SQLiteDatabase db, int langId) {
		if (!normalizeStatements.containsKey(langId)) {
			String sql = "UPDATE " + TableOperations.getWordsTable(langId) + " SET frequency /= ?";
			normalizeStatements.put(langId, db.compileStatement(sql));
		}

		return normalizeStatements.get(langId);
	}


	private static SQLiteStatement getChangeNormalizationScheduleStatement(@NonNull SQLiteDatabase db) {
		if (scheduleNormalizationStatement == null) {
			scheduleNormalizationStatement = db.compileStatement(
				"UPDATE " + TableOperations.LANGUAGES_META_TABLE + " SET normalizationPending = ? WHERE langId = ?"
			);
		}

		return scheduleNormalizationStatement;
	}


	public static boolean changeFrequency(@NonNull SQLiteDatabase db, @NonNull Language language, int position, int frequency) {
		SQLiteStatement query = getChangeFreqStatement(db, language);
		query.bindLong(1, frequency);
		query.bindLong(2, position);
		query.execute();

		Logger.d(LOG_TAG, "Change frequency SQL: " + query + "; (" + frequency + ", " + position + ")");

		return getAffectedRowsStatement(db).simpleQueryForLong() > 0;
	}


	public static void normalize(@NonNull SQLiteDatabase db, @NonNull SettingsStore settings, int langId) {
		if (langId <= 0) {
			return;
		}

		SQLiteStatement query = getNormalizeStatement(db, langId);
		query.bindLong(1, settings.getWordFrequencyNormalizationDivider());
		query.execute();

		query = getChangeNormalizationScheduleStatement(db);
		query.bindLong(1, langId);
		query.bindLong(2, 0);
		query.execute();
	}


	public static void scheduleNormalization(@NonNull SQLiteDatabase db, @NonNull Language language) {
		SQLiteStatement query = getChangeNormalizationScheduleStatement(db);
		query.bindLong(1, language.getId());
		query.bindLong(2, 1);
		query.execute();
	}
}
