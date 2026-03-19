package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.util.Logger;

abstract public class SQLiteOpener extends SQLiteOpenHelper {
	private static final String LOG_TAG = SQLiteOpener.class.getSimpleName();

	protected SQLiteDatabase db;


	public SQLiteOpener(@Nullable Context context, @NonNull String name) {
		super(context, name, null, BuildConfig.VERSION_CODE);
	}


	@NonNull abstract protected String[] getCreateQueries();
	@NonNull abstract Migration[] getMigrations();


	@Override
	public void onCreate(SQLiteDatabase db) {
		for (String query : getCreateQueries()) {
			db.execSQL(query);
		}
	}


	@Override
	public void onConfigure(SQLiteDatabase db) {
		super.onConfigure(db);
		setWriteAheadLoggingEnabled(true);
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
		for (Migration migration : getMigrations()) {
			if (oldVersion > migration.oldVersion()) {
				Logger.d(LOG_TAG, "Skipping migration: '" + migration.query() + "'. Highest previous version: " + migration.oldVersion() + " but we are at: " + oldVersion);
				continue;
			}

			try {
				db.execSQL(migration.query());
				Logger.d(LOG_TAG, "Migration succeeded: '" + migration.query());
			} catch (Exception e) {
				Logger.e(LOG_TAG, "Ignoring migration: '" + migration.query() + "'. ");
			}
		}
	}


	public SQLiteDatabase getDb() {
		if (db == null) {
			db = getWritableDatabase();
		}
		return db;
	}


	public void beginTransaction() {
		if (db != null) {
			db.beginTransactionNonExclusive();
		}
	}


	public void failTransaction() {
		if (db == null) {
			return;
		}

		if (db.inTransaction()) {
			db.endTransaction();
		} else {
			Logger.e(LOG_TAG, "Cannot fail a transaction when not in transaction.");
		}
	}


	public void finishTransaction() {
		if (db == null) {
			return;
		}

		if (db.inTransaction()) {
			db.setTransactionSuccessful();
			db.endTransaction();
		} else {
			Logger.e(LOG_TAG, "Cannot finish a transaction when not in transaction.");
		}
	}
}
