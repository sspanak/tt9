package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;



public class UpdateOperations {
	private static final String LOG_TAG = UpdateOperations.class.getSimpleName();
	private static SQLiteStatement affectedRowsStatement = null;
	private static final HashMap<Integer, SQLiteStatement> changeFreqStatements = new HashMap<>();

	private static SQLiteStatement getIncrementFrequencyStatement(@NonNull SQLiteDatabase db, @NonNull Language language) {
		if (!changeFreqStatements.containsKey(language.getId())) {
			String updateSQL = "UPDATE " + TableOperations.getWordsTable(language.getId()) + " SET frequency = ? WHERE position = ?";
			changeFreqStatements.put(language.getId(), db.compileStatement(updateSQL));
		}

		return changeFreqStatements.get(language.getId());
	}

	private static SQLiteStatement getAffectedRowsStatement(@NonNull SQLiteDatabase db) {
		return affectedRowsStatement != null ? affectedRowsStatement : (affectedRowsStatement = db.compileStatement("SELECT changes()"));
	}

	public static boolean changeFrequency(@NonNull SQLiteDatabase db, @NonNull Language language, int position, int frequency) {
		SQLiteStatement query = getIncrementFrequencyStatement(db, language);
		query.bindLong(1, frequency);
		query.bindLong(2, position);
		query.execute();

		Logger.d(LOG_TAG, "Change frequency SQL: " + query + "; (" + frequency + ", " + position + ")");

		return getAffectedRowsStatement(db).simpleQueryForLong() > 0;
	}
}
