package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

public class DeleteOps {
	public static void delete(@NonNull SQLiteDatabase db, int languageId) {
		db.delete(Tables.getWords(languageId), null, null);
		db.delete(Tables.getWordPositions(languageId), null, null);
	}

	public static void deleteCustomWord(@NonNull SQLiteDatabase db, int languageId, String word) {
		db.delete(Tables.getWords(languageId), "word = ?", new String[] { word });
		db.delete(Tables.CUSTOM_WORDS, "word = ?", new String[] { word });
	}

	public static int purgeCustomWords(@NonNull SQLiteDatabase db, int languageId) {
		String words = Tables.getWords(languageId);
		String positions = Tables.getWordPositions(languageId);

		String repeatingWordsSql = "SELECT GROUP_CONCAT(cw.ROWID) " +
			" FROM " + Tables.CUSTOM_WORDS + " AS cw " +
			" JOIN " + positions + " AS p ON p.sequence = cw.sequence " +
			" JOIN " + words + " AS w " +
				" ON w.position >= p.start AND w.position <= p.`end` " +
				" AND LOWER(w.word) = LOWER(cw.word) " +
			" WHERE cw.langId = ?";

		SQLiteStatement repeatingWordsQuery = CompiledQueryCache.get(db, repeatingWordsSql);
		repeatingWordsQuery.bindLong(1, languageId);
		String repeatingWords = repeatingWordsQuery.simpleQueryForString();

		return db.delete(Tables.CUSTOM_WORDS, "ROWID IN (" + repeatingWords + ")", null);
	}

	public static void deleteWordPairs(@NonNull SQLiteDatabase db, int languageId) {
		db.delete(Tables.getWordPairs(languageId), null, null);
	}
}
