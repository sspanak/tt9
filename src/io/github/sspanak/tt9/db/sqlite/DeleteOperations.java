package io.github.sspanak.tt9.db.sqlite;

import androidx.annotation.NonNull;

public class DeleteOperations {
	public static void delete(@NonNull SQLiteOpener sqlite, int languageId) {
		sqlite.getDb().delete(TableOperations.getWordsTable(languageId), null, null);
		sqlite.getDb().delete(TableOperations.getWordPositionsTable(languageId), null, null);
	}
}