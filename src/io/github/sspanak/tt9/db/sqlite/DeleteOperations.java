package io.github.sspanak.tt9.db.sqlite;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class DeleteOperations {
	public static void delete(@NonNull SQLiteOpener sqlite, int languageId) {
		sqlite.getDb().delete(TableOperations.getWordsTable(languageId), null, null);
		sqlite.getDb().delete(TableOperations.getWordPositionsTable(languageId), null, null);
	}

	public static void deleteMany(@NonNull SQLiteOpener sqlite, ArrayList<Integer> languageIds) {

		// tested and confirmed that multi-threading does not improve performance,
		// so we'll keep it simple
		for (int languageId : languageIds) {
			delete(sqlite, languageId);
		}
	}
}
