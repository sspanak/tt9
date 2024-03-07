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
}
