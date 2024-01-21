package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

public class DeleteOperations {
	public static void remove(@NonNull SQLiteOpener sqlite, int languageId) {
		if (sqlite == null) {
			return;
		}

		sqlite.runInTransaction(() -> {
			sqlite.getDb().delete(TableOperations.getWordsTable(languageId), null, null);
			sqlite.getDb().delete(TableOperations.getWordPositionsTable(languageId), null, null);
		});
	}
}
