package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.languages.EmojiLanguage;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class SQLiteOpener extends SQLiteOpenHelper {
	private static final String LOG_TAG = SQLiteOpener.class.getSimpleName();
	private static final String DATABASE_NAME = "tt9.db";
	private static final int DATABASE_VERSION = BuildConfig.VERSION_CODE;
	private static SQLiteOpener self;

	private final ArrayList<Language> allLanguages;
	private SQLiteDatabase db;


	private SQLiteOpener(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		allLanguages = new ArrayList<>(LanguageCollection.getAll(context));
		allLanguages.add(new EmojiLanguage());
	}


	public static SQLiteOpener getInstance(Context context) {
		if (self == null) {
			self = new SQLiteOpener(context);
		}

		return self;
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		for (String query : Tables.getCreateQueries(allLanguages)) {
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
		for (Migration migration : Migration.LIST) {
			try {
				db.execSQL(migration.query);
				Logger.d(LOG_TAG, "Migration succeeded: '" + migration.query);
			} catch (Exception e) {
				if (migration.mayFail) {
					Logger.e(LOG_TAG, "Ignoring migration: '" + migration.query + "'. ");
				} else {
					Logger.e(LOG_TAG, "Migration failed: '" + migration.query + "'. " + e.getMessage() + "\nAborting all subsequent migrations.");
					break;
				}
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
		if (db != null) {
			db.endTransaction();
		}
	}


	public void finishTransaction() {
		if (db != null) {
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}
}
