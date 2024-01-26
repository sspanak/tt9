package io.github.sspanak.tt9.db.sqlite;

import androidx.annotation.NonNull;

public class DeleteOps {
	public static void delete(@NonNull SQLiteOpener sqlite, int languageId) {
		sqlite.getDb().delete(Tables.getWords(languageId), null, null);
		sqlite.getDb().delete(Tables.getWordPositions(languageId), null, null);
	}
}
