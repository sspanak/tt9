package io.github.sspanak.tt9.db;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import io.github.sspanak.tt9.Logger;

public class LegacyDb {
	private static final String DB_NAME = "t9dict.db";
	private static final String TABLE_NAME = "words";

	private static boolean isCompleted = false;

	private final String LOG_TAG;
	private final Activity activity;
	private SQLiteDatabase db;

	public LegacyDb(Activity activity) {
		this.activity = activity;

		LOG_TAG = getClass().getSimpleName();
	}

	public void clear() {
		if (isCompleted) {
			return;
		}

		new Thread(() -> {
			openDb();
			if (areThereWords()) {
				deleteAll();
			}
			closeDb();
			isCompleted = true;
		}).start();
	}


	private void openDb() {
		try {
			db = null;
			File dbFile = activity.getDatabasePath(DB_NAME);
			if (dbFile.exists()) {
				db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
			}
		} catch (Exception e) {
			Logger.d(LOG_TAG, "Assuming no SQL database, because of error while opening. " + e.getMessage());
			db = null;
		}
	}


	private void closeDb() {
		if (db != null) {
			db.close();
		}
	}


	private boolean areThereWords() {
		String sql = "SELECT COUNT(*) FROM (SELECT id FROM " + TABLE_NAME + " LIMIT 1)";
		try (Cursor cursor = db.rawQuery(sql, null)) {
			return cursor.moveToFirst() && cursor.getInt(0) > 0;
		} catch (Exception e) {
			Logger.d(LOG_TAG, "Assuming no words, because of query error. " + e.getMessage());
			return false;
		}
	}

	private void deleteAll() {
		db.execSQL("DROP TABLE words");
		Logger.d(LOG_TAG, "SQL Words cleaned successfully.");
	}
}
