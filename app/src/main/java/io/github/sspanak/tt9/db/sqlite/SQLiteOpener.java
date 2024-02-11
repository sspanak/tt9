package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class SQLiteOpener extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "tt9.db";
	private static final int DATABASE_VERSION = BuildConfig.VERSION_CODE;
	private static SQLiteOpener self;

	private final ArrayList<Language> allLanguages;
	private SQLiteDatabase db;


	public SQLiteOpener(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		allLanguages = LanguageCollection.getAll(context);
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
