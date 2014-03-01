package org.nyanya.android.traditionalt9;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

public class T9DB {

	private static volatile T9DB instance = null;
	protected boolean ready = true;

	protected static final String DATABASE_NAME = "t9dict.db";
	protected static final int DATABASE_VERSION = 2;
	protected static final String WORD_TABLE_NAME = "word";
	protected static final String FREQ_TRIGGER_NAME = "freqtrigger";
	// 50k, 10k
	private static final int FREQ_MAX = 50000;
	private static final int FREQ_DIV = 10000;
	// This seems to be pretty fast on my phone. 10 is pretty slow (Might be because > MAX_RESULTS (8).)
	private static final int MINHITS = 4;

	protected static final String COLUMN_ID = BaseColumns._ID;
	protected static final String COLUMN_LANG = "lang";
	protected static final String COLUMN_SEQ = "seq";
	protected static final String COLUMN_WORD = "word";
	protected static final String COLUMN_FREQUENCY = "freq";

	private static final String QUERY1 = "SELECT " + COLUMN_ID + ", " + COLUMN_WORD + " FROM " + WORD_TABLE_NAME +
			" WHERE " + COLUMN_LANG + "=? AND " + COLUMN_SEQ + "=?" +
			" ORDER BY " + COLUMN_FREQUENCY + " DESC";

	private static final int MAX_RESULTS = 8;

	private static final int CAPS_OFF = 0;
	private static final int CAPS_SINGLE = 1;
	private static final int CAPS_ALL = 2;

	private DatabaseHelper mOpenHelper;
	private SQLiteDatabase db;

	private Context mContext;

	public T9DB(Context caller) {
		// create db
		mContext = caller;
		mOpenHelper = new DatabaseHelper(caller);
	}

	protected static T9DB getInstance(Context caller) {
		if (instance == null) {
			synchronized (T9DB.class){
				if (instance == null) {
					instance = new T9DB (caller);
					instance.init();
				}
			}
		}
		return instance;
	}

	protected static SQLiteDatabase getSQLDB(Context caller) {
		T9DB t9dbhelper = getInstance(caller);
		//Log.d("T9DB.getSQLDB", "db:" + t9dbhelper.db.isOpen());
		return t9dbhelper.db;
	}

	private void init() {
		if (mOpenHelper.needsUpgrading() ) {
			ready = false;
			Log.i("T9.init", "needsUpgrading");
			// start updating service
			if (db != null) {
				try {
				db.close();
				} catch (NullPointerException ignored) { }
				db = null;
			}
			Intent intent = new Intent(mContext, DBUpdateService.class);
			Log.i("T9.init", "Invoking update service...");
			mContext.startService(intent);

		} else {
			db = mOpenHelper.getWritableDatabase();
		}
	}

	protected boolean checkReady() {
		if (ready) {
			if (db == null) {
				db = getWritableDatabase();
			}
			return true;
		} else {
			return false;
		}
	}

	protected SQLiteDatabase getWritableDatabase() {
		return mOpenHelper.getWritableDatabase();
	}

	protected void setSQLDB(SQLiteDatabase tdb) {
		synchronized (T9DB.class){
			db = tdb;
			ready = true;
		}
	}

	protected void close() {
		try { db.close(); }
		catch (NullPointerException ignored) { }
		db = null;
	}

	protected void nuke() {
		Log.i("T9DB.nuke", "Deleting database...");
		synchronized (T9DB.class){
			if (db != null) {
				db.close();
			}
			if (!mContext.deleteDatabase(DATABASE_NAME)) {
				Log.e("T9DB", "Couldn't delete database.");
			}
			Log.i("T9DB.nuke", "Preparing database...");
			getWritableDatabase().close();

			db = null;
			ready = true;
			init();
		}
		Log.i("T9DB.nuke", "Done...");
	}

	protected void addWord(String iword, int lang) throws DBException {
		Resources r = mContext.getResources();
		if (iword.equals("")) {
			throw new DBException(r.getString(R.string.add_word_blank));
		}
		// get int sequence
		String seq;
		try {
			seq = CharMap.getStringSequence(iword, lang);
		} catch (NullPointerException e) {
			throw new DBException(r.getString(R.string.add_word_badchar, LangHelper.LANGS[lang]));
		}
		// add int sequence into num table
		ContentValues values = new ContentValues();
		values.put(COLUMN_SEQ, seq);
		values.put(COLUMN_LANG, lang);
		// add word into word
		values.put(COLUMN_WORD, iword);
		values.put(COLUMN_FREQUENCY, 1);
		if (!checkReady()) {
			Log.e("T9DB.addWord", "not ready");
			Toast.makeText(mContext, R.string.database_notready, Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			db.insertOrThrow(WORD_TABLE_NAME, null, values);
		} catch (SQLiteConstraintException e) {
			String msg = r.getString(R.string.add_word_exist2, iword, LangHelper.LANGS[lang]);
			Log.w("T9DB.addWord", msg);
			throw new DBException(msg);
		}
	}

	protected void incrementWord(int id) {
		if (!checkReady()) {
			Log.e("T9DB.incrementWord", "not ready");
			Toast.makeText(mContext, R.string.database_notready, Toast.LENGTH_SHORT).show();
			return;
		}
		db.execSQL(
				"UPDATE " + WORD_TABLE_NAME +
						" SET " + COLUMN_FREQUENCY + " = " + COLUMN_FREQUENCY + "+ 1" +
						" WHERE " + COLUMN_ID + " = \"" + id + "\"");
		// if id's freq is greater than FREQ_MAX, it gets normalized with trigger
	}

	protected String getWord(String is, int lang) {
		String result = null;
		String q =
				"SELECT " + COLUMN_WORD + " FROM " + WORD_TABLE_NAME +
						" WHERE " + COLUMN_LANG + "=? AND " + COLUMN_SEQ + "=?" +
						" ORDER BY " + COLUMN_FREQUENCY + " DESC";
		if (!checkReady()) {
			Log.e("T9DB.getWord", "not ready");
			Toast.makeText(mContext, R.string.database_notready, Toast.LENGTH_SHORT).show();
			return "";
		}
		Cursor cur = db.rawQuery(q, new String[] { is, String.valueOf(lang) });
		int hits = 0;
		if (cur.moveToFirst()) {
			result = cur.getString(0);
		}
		cur.close();
		if (result != null) {
			return result;
		} else {
			int islen = is.length();
			char c = is.charAt(islen - 1);
			c++;
			q = "SELECT " + COLUMN_WORD + " FROM " + WORD_TABLE_NAME +
					" WHERE " + COLUMN_LANG + "=? AND " + COLUMN_SEQ + " >= '" + is + "1" +
					"' AND " + COLUMN_SEQ + " < '" + is.substring(0, islen - 1) + c + "'" +
					" ORDER BY " + COLUMN_FREQUENCY	+ " DESC, " + COLUMN_SEQ + " ASC" +
					" LIMIT " + (MAX_RESULTS - hits);
			cur = db.rawQuery(q, new String[] { String.valueOf(lang) });

			if (cur.moveToFirst()) {
				result = cur.getString(0);
			}
			if (result == null) {
				result = "";
			}
			cur.close();
		}
		return result;
	}

	protected void updateWords(String is, AbstractList<String> stringList, List<Integer> intList,
							int capsMode, int lang) {
		stringList.clear();
		intList.clear();
		// String[] sa = packInts(stringToInts(is), true);
		int islen = is.length();

		if (!checkReady()) {
			Log.e("T9DB.updateWords", "not ready");
			Toast.makeText(mContext, R.string.database_notready, Toast.LENGTH_SHORT).show();
			return;
		}
		Cursor cur = db.rawQuery(QUERY1, new String[] { String.valueOf(lang), is  });

		int hits = 0;
		for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			intList.add(cur.getInt(0));
			stringList.add(cur.getString(1));
//			if (hits >= 15) {
//				break;
//			}
			hits++;
		}
		cur.close();

		if (hits < MINHITS) {
			char c = is.charAt(islen - 1);
			c++;
			String q = "SELECT " + COLUMN_ID + ", " + COLUMN_WORD +
					" FROM " + WORD_TABLE_NAME +
					" WHERE " + COLUMN_LANG + "=? AND " + COLUMN_SEQ + " >= '" + is + "1" +
					"' AND " + COLUMN_SEQ + " < '" + is.substring(0, islen - 1) + c + "'" +
					" ORDER BY " + COLUMN_FREQUENCY	+ " DESC, " + COLUMN_SEQ + " ASC" +
					" LIMIT " + (MAX_RESULTS - hits);
			cur = db.rawQuery(q, new String[] { String.valueOf(lang) });

			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				intList.add(cur.getInt(0));
				stringList.add(cur.getString(1));
				if (hits >= 20) {
					break;
				}
				hits++;
			}
			cur.close();
		}
		// Log.d("T9DB.updateWords", "pre: " + stringList);
		if (capsMode == CAPS_OFF) {
			return;
		}
		// Log.d("T9DB.updateWords", "filtering...");
		// filter list
		Iterator<String> iter = stringList.iterator();
		String word;
		String wordtemp;
		int index = 0;
		boolean removed = false;
		while (iter.hasNext()) {
			word = iter.next();
			switch (capsMode) {
				case CAPS_ALL:
					wordtemp = word.toUpperCase(LangHelper.LOCALES[lang]);
					if (wordtemp.equals(word)) {
						index++;
						continue;
					} else if (stringList.contains(wordtemp)) {
						// remove this entry
						iter.remove();
						removed = true;
					} else {
						stringList.set(index, wordtemp);
					}
					break;
				case CAPS_SINGLE:
					if (word.length() > 1) {
						wordtemp = word.substring(0, 1).toUpperCase(LangHelper.LOCALES[lang]) + word.substring(1);
					} else {
						wordtemp = word.toUpperCase(LangHelper.LOCALES[lang]);
					}
					if (wordtemp.equals(word)) {
						index++;
						continue;
					} else if (stringList.contains(wordtemp)) {
						// remove this entry
						iter.remove();
						removed = true;
					} else {
						stringList.set(index, wordtemp);
					}
					break;
			}
			if (removed) {
				intList.remove(index);
				removed = false;
			} else {
				index++;
			}
		}
		//Log.d("T9DB.updateWords", "i:" + is + " words:" + Arrays.toString(stringList.toArray()));
	}

	protected void updateWordsW(String is, Collection<String> stringList,
								Collection<Integer> intList, Collection<Integer> freq, int lang) {
		stringList.clear();
		intList.clear();
		freq.clear();
		// String[] sa = packInts(stringToInts(is), true);
		int islen = is.length();
		String q = "SELECT " + COLUMN_ID + ", " + COLUMN_WORD + ", " + COLUMN_FREQUENCY +
				" FROM " + WORD_TABLE_NAME +
				" WHERE " + COLUMN_LANG + "=? AND " + COLUMN_SEQ + "=?" +
				" ORDER BY " + COLUMN_FREQUENCY + " DESC";
		if (!checkReady()) {
			Log.e("T9DB.updateWordsW", "not ready");
			Toast.makeText(mContext, R.string.database_notready, Toast.LENGTH_SHORT).show();
			return;
		}
		Cursor cur = db.rawQuery(q, new String[] { is, String.valueOf(lang) });
		int hits = 0;
		for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			intList.add(cur.getInt(0));
			stringList.add(cur.getString(1));
			freq.add(cur.getInt(2));
			if (hits >= 10) {
				break;
			}
			hits++;
		}
		cur.close();
		if (hits < MINHITS) {
			char c = is.charAt(islen - 1);
			c++;
			q = "SELECT " + COLUMN_ID + ", " + COLUMN_WORD + ", " + COLUMN_FREQUENCY +
					" FROM " + WORD_TABLE_NAME +
					" WHERE " + COLUMN_LANG + "=? AND " + COLUMN_SEQ + " >= '" + is +
					"' AND " + COLUMN_SEQ + " < '" + is.substring(0, islen - 1) + c + "'" +
					" ORDER BY " + COLUMN_FREQUENCY + " DESC, " + COLUMN_SEQ + " ASC" +
					" LIMIT " + (MAX_RESULTS - hits);
			cur = db.rawQuery(q, new String[] { String.valueOf(lang) });

			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				intList.add(cur.getInt(0));
				stringList.add(cur.getString(1));
				freq.add(cur.getInt(2));
				if (hits >= 10) {
					break;
				}
				hits++;
			}
			cur.close();
		}
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		Context mContext = null;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}

		// partial code from parent class SQLiteOpenHelper
		protected boolean needsUpgrading() {
			//quick and dirty check to see if an existing database exists.
			if (mContext.databaseList().length > 0) {
				SQLiteDatabase db = mContext.openOrCreateDatabase(DATABASE_NAME, 0, null);
				int version = db.getVersion();
				db.close();
				return version < DATABASE_VERSION;
			} else {
				return false;
			}
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + WORD_TABLE_NAME + " (" +
					COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					COLUMN_LANG + " INTEGER, " +
					COLUMN_SEQ + " TEXT, " +
					COLUMN_WORD	+ " TEXT, " +
					COLUMN_FREQUENCY + " INTEGER, " +
					"UNIQUE(" + COLUMN_LANG + ", " + COLUMN_WORD + ") )");
			db.execSQL("CREATE INDEX idx ON " + WORD_TABLE_NAME + "("
					+ COLUMN_LANG + ", " + COLUMN_SEQ + " ASC, " + COLUMN_FREQUENCY + " DESC )");
			db.execSQL("CREATE TRIGGER IF NOT EXISTS " + FREQ_TRIGGER_NAME +
					" AFTER UPDATE ON " + WORD_TABLE_NAME +
					" WHEN NEW." + COLUMN_FREQUENCY + " > " + FREQ_MAX +
					" BEGIN" +
					" UPDATE " + WORD_TABLE_NAME + " SET " + COLUMN_FREQUENCY + " = "
					+ COLUMN_FREQUENCY + " / " + FREQ_DIV +
					" WHERE " + COLUMN_SEQ + " = NEW." + COLUMN_SEQ + ";" +
					" END;");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("T9DB.onUpgrade", "Upgrading database from version " + oldVersion + " to " + newVersion);
			if (oldVersion == 1) {
				db.execSQL("ALTER TABLE " + WORD_TABLE_NAME + " ADD COLUMN " +
						COLUMN_LANG + " INTEGER");
				db.execSQL("DROP INDEX IF EXISTS idx");
				onCreate(db);
				ContentValues updatedata = new ContentValues();
				updatedata.put(COLUMN_LANG, 0);
				db.update(WORD_TABLE_NAME, updatedata, null, null);
			}
			Log.i("T9DB.onUpgrade", "Done.");
		}
	}
}
