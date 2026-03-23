package io.github.sspanak.tt9.db;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.util.Logger;

abstract public class BaseSyncStore {
	protected SQLiteOpener sqlite;

	protected BaseSyncStore(Context context) {
		try {
			sqlite = openDb(context);
			sqlite.getDb();
		} catch (Exception e) {
			sqlite = null;
			Logger.w(getClass().getSimpleName(), "Database connection failure. All operations will return empty results. " + e.getMessage());
		}
	}


	@NonNull
	abstract protected SQLiteOpener openDb(Context context);


	protected boolean checkOrNotify() {
		if (sqlite == null || sqlite.getDb() == null) {
			Logger.e(getClass().getSimpleName(), "No database connection. Cannot query any data.");
			return false;
		}

		return true;
	}

	public void startTransaction() {
		if (sqlite != null) {
			sqlite.beginTransaction();
		}
	}

	public void failTransaction() {
		if (sqlite != null) {
			sqlite.failTransaction();
		}
	}

	public void finishTransaction() {
		if (sqlite != null) {
			sqlite.finishTransaction();
		}
	}
}
