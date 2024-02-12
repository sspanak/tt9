package io.github.sspanak.tt9.db;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.sspanak.tt9.Logger;

public class LegacyDb extends SQLiteOpenHelper {
	private final String LOG_TAG = getClass().getSimpleName();
	private static final String DB_NAME = "t9dict.db";
	private static final String TABLE_NAME = "words";

	private static boolean isCompleted = false;

	public LegacyDb(Activity activity) {
		super(activity.getApplicationContext(), DB_NAME, null, 12);
	}

	public void clear() {
		if (isCompleted) {
			return;
		}

		new Thread(() -> {
			try (SQLiteDatabase db = getWritableDatabase()) {
				db.compileStatement("DROP TABLE " + TABLE_NAME).execute();
				Logger.d(LOG_TAG, "SQL Words cleaned successfully.");
			} catch (Exception e) {
				Logger.d(LOG_TAG, "Assuming no words, because of query error. " + e.getMessage());
			} finally {
				isCompleted = true;
			}
		}).start();
	}

	@Override public void onCreate(SQLiteDatabase db) {}
	@Override public void onUpgrade(SQLiteDatabase db, int i, int ii) {}
}
