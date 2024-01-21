package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.github.sspanak.tt9.languages.Language;



public class UpdateOperations {
	private static SQLiteStatement affectedRowsStatement = null;
	private static final HashMap<Integer, SQLiteStatement> incrementFreqStatements = new HashMap<>();

	private static SQLiteStatement getIncrementFrequencyStatement(@NonNull SQLiteDatabase db, @NonNull Language language) {
		if (!incrementFreqStatements.containsKey(language.getId())) {
			String maxFrequencySQL = "SELECT MAX(frequency) FROM " + TableOperations.getWordsTable(language.getId()) +
				" WHERE " +
					"word <> ? " +
					"AND position BETWEEN " +
					"(SELECT `start` FROM " + TableOperations.getWordPositionsTable(language.getId()) + " wp WHERE wp.sequence = ?) " +
					" AND (SELECT `end` FROM " + TableOperations.getWordPositionsTable(language.getId()) + " wp WHERE wp.sequence = ?) ";

			String updateSQL = "UPDATE " + TableOperations.getWordsTable(language.getId()) +
				" SET frequency = (" + maxFrequencySQL + ") + 1 " +
				" WHERE word = ?";

			incrementFreqStatements.put(language.getId(), db.compileStatement(updateSQL));
		}

		return incrementFreqStatements.get(language.getId());
	}

	private static SQLiteStatement getAffectedRowsStatement(@NonNull SQLiteDatabase db) {
		return affectedRowsStatement != null ? affectedRowsStatement : (affectedRowsStatement = db.compileStatement("SELECT changes()"));
	}

	public static boolean incrementFrequency(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String word, @NonNull String sequence) {
		SQLiteStatement compiled = getIncrementFrequencyStatement(db, language);
		compiled.bindString(1, word);
		compiled.bindString(2, sequence);
		compiled.bindString(3, sequence);
		compiled.bindString(4, word);
		compiled.execute();

		return getAffectedRowsStatement(db).simpleQueryForLong() > 0;
	}
}
