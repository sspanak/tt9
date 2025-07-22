package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;

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

	public static void purgeCustomWords(@NonNull SQLiteDatabase db, int languageId) {
		String words = Tables.getWords(languageId);

		String repeatingWords =
			"SELECT " + Tables.CUSTOM_WORDS + ".ROWID FROM " + Tables.CUSTOM_WORDS +
			" JOIN " + words + " ON LOWER(" + words + ".word) = LOWER(" + Tables.CUSTOM_WORDS + ".word) " +
			" WHERE langId = " + languageId + " AND " + words + ".position > 0";

		db.delete(Tables.CUSTOM_WORDS, "ROWID IN (" + repeatingWords + ")", null);
	}

	public static void deleteWordPairs(@NonNull SQLiteDatabase db, int languageId) {
		db.delete(Tables.getWordPairs(languageId), null, null);
	}
}
