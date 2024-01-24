package io.github.sspanak.tt9.db;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import io.github.sspanak.tt9.Logger;

public class LegacyDb extends SQLiteOpenHelper {
	private final String LOG_TAG = getClass().getSimpleName();
	private static final String DB_NAME = "t9dict.db";
	private static final String TABLE_NAME = "words";

	private static boolean isCompleted = false;

	private SQLiteDatabase db;

	private SQLiteStatement deleteAllQuery = null;
	private SQLiteStatement existsQuery = null;

	public LegacyDb(Activity activity) {
		super(activity.getApplicationContext(), DB_NAME, null, 1);
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
			db = getWritableDatabase();
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
		try {
			return existsQuery.simpleQueryForLong() > 0;
		} catch (Exception e) {
			Logger.d(LOG_TAG, "Assuming no words, because of query error. " + e.getMessage());
			return false;
		}
	}

	private void deleteAll() {
		deleteAllQuery.execute();
		Logger.d(LOG_TAG, "SQL Words cleaned successfully.");
	}

	@Override public void onCreate(SQLiteDatabase db) {
		deleteAllQuery = db.compileStatement("DROP TABLE " + TABLE_NAME);
		existsQuery = db.compileStatement("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = '" + TABLE_NAME + "'");
	}
	@Override public void onUpgrade(SQLiteDatabase db, int i, int ii) {}
}
