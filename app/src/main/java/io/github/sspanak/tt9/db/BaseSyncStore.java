package io.github.sspanak.tt9.db;

import android.content.Context;

import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.util.Logger;

public class BaseSyncStore {
	protected SQLiteOpener sqlite;

	protected BaseSyncStore(Context context) {
		try {
			sqlite = SQLiteOpener.getInstance(context);
			sqlite.getDb();
		} catch (Exception e) {
			sqlite = null;
			Logger.w(getClass().getSimpleName(), "Database connection failure. All operations will return empty results. " + e.getMessage());
		}
	}

	protected boolean checkOrNotify() {
		if (sqlite == null || sqlite.getDb() == null) {
			Logger.e(getClass().getSimpleName(), "No database connection. Cannot query any data.");
			return false;
		}

		return true;
	}
}
