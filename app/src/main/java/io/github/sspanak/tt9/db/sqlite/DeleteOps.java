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
		db.delete(Tables.CUSTOM_WORD_READINGS, "customId IN (SELECT id FROM " + Tables.CUSTOM_WORDS + " WHERE langId = ? AND word = ?)",
			new String[]{String.valueOf(languageId), word});
		db.delete(Tables.getWords(languageId), "word = ? AND position < 0", new String[] { word });
		db.delete(Tables.CUSTOM_WORDS, "langId = ? AND word = ?", new String[] { String.valueOf(languageId), word });
	}

	public static int purgeCustomWords(@NonNull SQLiteDatabase db, int languageId) {
		// Chinese entries are also explicit personal reading and rare-character overrides.
		if (languageId == 462106) return 0;
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

		int deleted = db.delete(Tables.CUSTOM_WORDS, "ROWID IN (" + repeatingWords + ")", null);
		db.delete(Tables.CUSTOM_WORD_READINGS, "customId NOT IN (SELECT id FROM " + Tables.CUSTOM_WORDS + ")", null);
		return deleted;
	}

	public static void deleteWordPairs(@NonNull SQLiteDatabase db, int languageId) {
		db.delete(Tables.getWordPairs(languageId), null, null);
	}

	public static void deleteMindReaderNgrams(@NonNull SQLiteDatabase db, int languageId) {
		db.delete(Tables.getMindReaderNgrams(languageId), null, null);
	}

	public static void deleteMindReaderTokens(@NonNull SQLiteDatabase db, int languageId) {
		db.delete(Tables.getMindReaderTokens(languageId), null, null);
	}
}
