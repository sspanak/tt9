package io.github.sspanak.tt9.dblegacy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	Context mContext = null;

	DatabaseHelper(Context context) {
		super(context, T9DB.DATABASE_NAME, null, T9DB.DATABASE_VERSION);
		mContext = context;
	}

	// partial code from parent class SQLiteOpenHelper
	protected boolean needsUpgrading() {
		//quick and dirty check to see if an existing database exists.
		if (mContext.databaseList().length > 0) {
			SQLiteDatabase db = mContext.openOrCreateDatabase(T9DB.DATABASE_NAME, 0, null);
			int version = db.getVersion();
			db.close();
			return version < T9DB.DATABASE_VERSION;
		} else {
			return false;
		}
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + T9DB.WORD_TABLE_NAME + " (" +
				T9DB.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				T9DB.COLUMN_LANG + " INTEGER, " +
				T9DB.COLUMN_SEQ + " TEXT, " +
				T9DB.COLUMN_WORD	+ " TEXT, " +
				T9DB.COLUMN_FREQUENCY + " INTEGER, " +
				"UNIQUE(" + T9DB.COLUMN_LANG + ", " + T9DB.COLUMN_WORD + ") )");
		db.execSQL("CREATE INDEX IF NOT EXISTS idx ON " + T9DB.WORD_TABLE_NAME + "("
				+ T9DB.COLUMN_LANG + ", " + T9DB.COLUMN_SEQ + " ASC, " + T9DB.COLUMN_FREQUENCY + " DESC )");
		db.execSQL("CREATE TRIGGER IF NOT EXISTS " + T9DB.FREQ_TRIGGER_NAME +
				" AFTER UPDATE ON " + T9DB.WORD_TABLE_NAME +
				" WHEN NEW." + T9DB.COLUMN_FREQUENCY + " > " + T9DB.FREQ_MAX +
				" BEGIN" +
				" UPDATE " + T9DB.WORD_TABLE_NAME + " SET " + T9DB.COLUMN_FREQUENCY + " = "
				+ T9DB.COLUMN_FREQUENCY + " / " + T9DB.FREQ_DIV +
				" WHERE " + T9DB.COLUMN_SEQ + " = NEW." + T9DB.COLUMN_SEQ + ";" +
				" END;");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("T9DB.onUpgrade", "Upgrading database from version " + oldVersion + " to " + newVersion);
		onCreate(db);
		// subsequent database migrations go here
		Log.i("T9DB.onUpgrade", "Done.");
	}
}
