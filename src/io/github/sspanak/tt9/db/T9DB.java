package io.github.sspanak.tt9.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import io.github.sspanak.tt9.LangHelper;
import io.github.sspanak.tt9.LangHelper.LANGUAGE;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.T9Preferences;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class T9DB {

	private static volatile T9DB instance = null;
	protected boolean ready = true;

	public static final String DATABASE_NAME = "t9dict.db";
	public static final int DATABASE_VERSION = 5;	// Versions < 5 belong to the original project. We don't care about
																										// them and we don't migrate them, because the APP ID used to be
																										// different. This means the TT9 must be installed as a new application
																										// since version 5, which eliminates the possibility of reusing any
																										// legacy data.
	public static final String WORD_TABLE_NAME = "word";
	public static final String FREQ_TRIGGER_NAME = "freqtrigger";
	// 50k, 10k
	public static final int FREQ_MAX = 50000;
	public static final int FREQ_DIV = 10000;
	// This seems to be pretty fast on my phone. 10 is pretty slow (Might be because > MAX_RESULTS (8).)
	private static final int MINHITS = 4;

	public static final String COLUMN_ID = BaseColumns._ID;
	public static final String COLUMN_LANG = "lang";
	public static final String COLUMN_SEQ = "seq";
	public static final String COLUMN_WORD = "word";
	public static final String COLUMN_FREQUENCY = "freq";

	private static final String QUERY1 =
		"SELECT " + COLUMN_ID + ", " + COLUMN_WORD +
		" FROM " + WORD_TABLE_NAME +
		" WHERE " + COLUMN_LANG + "=? AND " + COLUMN_SEQ + "=?" +
		" ORDER BY " + COLUMN_FREQUENCY + " DESC";

	private static final String UPDATEQ =
		"UPDATE " + WORD_TABLE_NAME +
		" SET " + COLUMN_FREQUENCY + " = " + COLUMN_FREQUENCY + "+1" +
		" WHERE " + COLUMN_ID + "=";

	private static final int MAX_RESULTS = 8;
	private static final int MAX_MAX_RESULTS = 30; // to make sure we don't exceed candidate view array.

	private final DatabaseHelper mOpenHelper;
	private SQLiteDatabase db;

	private final Context mContext;

	public T9DB(Context caller) {
		// create db
		mContext = caller;
		mOpenHelper = new DatabaseHelper(caller);
	}

	public static T9DB getInstance(Context caller) {
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

	public static SQLiteDatabase getSQLDB(Context caller) {
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

	private boolean ensureDb() {
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

	public boolean isReady() {
		return this.ready;
	}

	public void close() {
		try { db.close(); }
		catch (NullPointerException ignored) { }
		db = null;
	}

	public void truncate() {
		Log.i("T9DB.truncate", "Truncating words table...");
		synchronized (T9DB.class) {
			ready = false;
			db = getWritableDatabase();
			db.delete(WORD_TABLE_NAME, null, null);
			ready = true;
		}
		Log.i("T9DB.truncate", "Done...");
	}

	public void showDBaccessError() {
		Toast.makeText(mContext, R.string.database_notready, Toast.LENGTH_SHORT).show();
	}

	public void addWord(String iword, Language lang) throws DBException {
		Resources r = mContext.getResources();
		if (iword.equals("")) {
			throw new DBException(r.getString(R.string.add_word_blank));
		}
		// get int sequence
		String seq;
		try {
			seq = lang.getDigitSequenceForWord(iword);
		} catch (Exception e) {
			throw new DBException(r.getString(R.string.add_word_badchar, lang.getName(), iword));
		}
		// add int sequence into num table
		ContentValues values = new ContentValues();
		values.put(COLUMN_SEQ, seq);
		values.put(COLUMN_LANG, lang.getId());
		// add word into word
		values.put(COLUMN_WORD, iword);
		values.put(COLUMN_FREQUENCY, 1);
		if (!ensureDb()) {
			Log.e("T9DB.addWord", "not ready");
			this.showDBaccessError();
			return;
		}
		try {
			db.insertOrThrow(WORD_TABLE_NAME, null, values);
			Log.i("T9DB.addWord", "Added: " + iword + ", for language: " + lang.getId());
		} catch (SQLiteConstraintException e) {
			String msg = r.getString(R.string.add_word_exist2, iword, lang.getName());
			Log.w("T9DB.addWord", msg);
			throw new DBException(msg);
		}
	}

	public void incrementWord(int id) {
		if (!ensureDb()) {
			Log.e("T9DB.incrementWord", "not ready");
			this.showDBaccessError();
			return;
		}
		db.execSQL(UPDATEQ + id);
		// if id's freq is greater than FREQ_MAX, it gets normalized with trigger
	}

	public void updateWords(String is, AbstractList<String> stringList, List<Integer> intList,
							int capsMode, LANGUAGE lang) {
		stringList.clear();
		intList.clear();
		// String[] sa = packInts(stringToInts(is), true);
		int islen = is.length();

		if (!ensureDb()) {
			Log.e("T9DB.updateWords", "not ready");
			this.showDBaccessError();
			return;
		}
		Cursor cur = db.rawQuery(QUERY1, new String[] { String.valueOf(lang.id), is  });

		int hits = 0;
		for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			intList.add(cur.getInt(0));
			stringList.add(cur.getString(1));
			if (hits >= MAX_MAX_RESULTS) { break; } // to stop index error in candidate view
			hits++;
		}
		cur.close();

		if ((hits < MINHITS) && (islen >= 2)) {
			char c = is.charAt(islen - 1);
			c++;
			String q = "SELECT " + COLUMN_ID + ", " + COLUMN_WORD +
					" FROM " + WORD_TABLE_NAME +
					" WHERE " + COLUMN_LANG + "=? AND " + COLUMN_SEQ + " >= '" + is + "1" +
					"' AND " + COLUMN_SEQ + " < '" + is.substring(0, islen - 1) + c + "'" +
					" ORDER BY " + COLUMN_FREQUENCY	+ " DESC, " + COLUMN_SEQ + " ASC" +
					" LIMIT " + (MAX_RESULTS - hits);
			cur = db.rawQuery(q, new String[] { String.valueOf(lang.id) });

			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				intList.add(cur.getInt(0));
				stringList.add(cur.getString(1));
				if (hits >= MAX_MAX_RESULTS) {
					break;
				}
				hits++;
			}
			cur.close();
		}
		// Log.d("T9DB.updateWords", "pre: " + stringList);
		if (capsMode == T9Preferences.CASE_LOWER) {
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
				case T9Preferences.CASE_UPPER:
					wordtemp = word.toUpperCase(LangHelper.LOCALES[lang.index]);
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
				case T9Preferences.CASE_CAPITALIZE:
					if (word.length() > 1) {
						wordtemp = word.substring(0, 1).toUpperCase(LangHelper.LOCALES[lang.index]) + word.substring(1);
					} else {
						wordtemp = word.toUpperCase(LangHelper.LOCALES[lang.index]);
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
}
