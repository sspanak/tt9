package org.nyanya.android.traditionalt9;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.nyanya.android.traditionalt9.LangHelper.LANGUAGE;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class T9DB {

	private static volatile T9DB instance = null;
	protected boolean ready = true;

	protected static final String DATABASE_NAME = "t9dict.db";
	protected static final int DATABASE_VERSION = 4;
	protected static final String WORD_TABLE_NAME = "word";
	protected static final String SETTING_TABLE_NAME = "setting";
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

	private static final String UPDATEQ = "UPDATE " + WORD_TABLE_NAME +
			" SET " + COLUMN_FREQUENCY + " = " + COLUMN_FREQUENCY + "+1" +
			" WHERE " + COLUMN_ID + "=";

	private static final int MAX_RESULTS = 8;
	private static final int MAX_MAX_RESULTS = 30; // to make sure we don't exceed candidate view array.

	private static final int CAPS_OFF = 0;
	private static final int CAPS_SINGLE = 1;
	private static final int CAPS_ALL = 2;

	private DatabaseHelper mOpenHelper;
	private SQLiteDatabase db;

	private Context mContext;

	public static class DBSettings {
		public enum SETTING {
			INPUT_MODE("pref_inputmode", 0, 0),
			LANG_SUPPORT("pref_lang_support", 1, 1),
			MODE_NOTIFY("pref_mode_notify", 0, 2),
			LAST_LANG("set_last_lang", 1, 5),
			LAST_WORD("set_last_word", null, 6),
			SPACE_ZERO("pref_spaceOnZero", 0, 4),
			KEY_REMAP("pref_keyMap", 0, 3);

			public final String id;
			public final Integer defvalue;
			public final int sqOrder; // used for building SettingsUI

			// lookup map
			private static final Map<String, SETTING> lookup = new HashMap<String, SETTING>();
			private static final SETTING[] settings = SETTING.values();
			static { for (SETTING l : settings) lookup.put(l.id, l); }

			private SETTING(String id, Integer defval, int sqOrder) {
				this.id = id; this.defvalue = defval; this.sqOrder = sqOrder;
			}

			public static SETTING get(String i) { return lookup.get(i);}
			public static StringBuilder join(SETTING[] settings, StringBuilder sb) {
				for (int x=0; x<settings.length; x++) {
					sb.append(settings[x].id);
					if (x < settings.length-1)
						sb.append(", ");
				}
				return sb;
			}
		}
		protected static final String SETTINGQUERY = " FROM " + SETTING_TABLE_NAME +
				" WHERE " + COLUMN_ID + "=1";
	}

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

	public boolean checkReady() {
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
			String[] oldSettings = getSettings();
			if (oldSettings == null) { Log.e("T9DB", "Couldn't get old settings"); }
			if (db != null) { db.close(); }
			if (!mContext.deleteDatabase(DATABASE_NAME)) { Log.e("T9DB", "Couldn't delete database."); }
			Log.i("T9DB.nuke", "Preparing database...");
			getWritableDatabase().close();

			db = null;
			ready = true;
			init();
			if (oldSettings != null) {
				StringBuilder sb = new StringBuilder("INSERT OR REPLACE INTO ");
				sb.append(SETTING_TABLE_NAME); sb.append(" (");	sb.append(COLUMN_ID); sb.append(",");
				sb = DBSettings.SETTING.join(DBSettings.SETTING.settings, sb);
				sb.append(") VALUES ("); sb.append(TextUtils.join(",", oldSettings)); sb.append(")");
				db.execSQL(sb.toString());
			}
		}
		Log.i("T9DB.nuke", "Done...");
	}

	public void showDBaccessError() {
		Toast.makeText(mContext, R.string.database_notready, Toast.LENGTH_SHORT).show();
	}

	public boolean storeSettingString(DBSettings.SETTING key, String value) {
		ContentValues updatedata = new ContentValues();
		updatedata.put(key.id, value);
		return storeSetting(updatedata);
	}

	public boolean storeSettingInt(DBSettings.SETTING key, int value) {
		ContentValues updatedata = new ContentValues();
		updatedata.put(key.id, value);
		return storeSetting(updatedata);
	}

	public boolean storeSetting(ContentValues updatedata) {
		if (!checkReady()) {
			Log.e("T9DB.storeSetting", "not ready");
			return false;
		}
		db.update(SETTING_TABLE_NAME, updatedata, null, null);
		return true;
	}

	// CHECK READY BEFORE CALLING THIS SO CAN SHOW USER MESSAGE IF NOT READY
	public int getSettingInt(DBSettings.SETTING key) {
		Cursor cur = db.rawQuery((new StringBuilder("SELECT ")).append(key.id)
				.append(DBSettings.SETTINGQUERY).toString(), null);
		if (cur.moveToFirst()) {
			int value = cur.getInt(0);
			cur.close();
			return value;
		}
		return key.defvalue;
	}
	public String getSettingString(DBSettings.SETTING key) {
		if (!checkReady()) {
			return null;
		}
		Cursor cur = db.rawQuery((new StringBuilder("SELECT ")).append(key.id)
				.append(DBSettings.SETTINGQUERY).toString(), null);
		if (cur.moveToFirst()) {
			String value = cur.getString(0);
			cur.close();
			return value;
		}
		return null;
	}


	public Object[] getSettings(DBSettings.SETTING[] keys) {
		if (checkReady()) {
			StringBuilder sb = new StringBuilder("SELECT ");
			sb = DBSettings.SETTING.join(keys, sb);
			Cursor cur = db.rawQuery(sb.append(DBSettings.SETTINGQUERY).toString(), null);
			if (cur.moveToFirst()) {
				Object[] values = new Object[keys.length];
				for (int x=0;x<keys.length;x++){
					if (keys[x] == DBSettings.SETTING.LAST_WORD)
						values[x] = cur.getString(x);
					else
						values[x] = cur.getInt(x);
				}
				cur.close();
				return values;
			}
		} else {
			Log.e("T9DB.getSettings", "not ready");
			Toast.makeText(mContext, R.string.database_settings_notready, Toast.LENGTH_SHORT).show();
		}
		Object[] values = new Object[keys.length];
		for (int x=0;x<keys.length;x++) {
			values[x] = keys[x].defvalue;
		}
		return values;
	}

	private String[] getSettings() {
		if (!checkReady()) {
			Log.e("T9DB.getSetting", "not ready");
			return null;
		}
		int len = DBSettings.SETTING.settings.length+1;
		String[] settings = new String[len];
		StringBuilder sb = new StringBuilder("SELECT "); sb.append(COLUMN_ID); sb.append(",");
		sb = DBSettings.SETTING.join(DBSettings.SETTING.settings, sb);
		sb.append(" FROM "); sb.append(SETTING_TABLE_NAME); sb.append(" WHERE "); sb.append(COLUMN_ID);
		sb.append("=1");

		Cursor cur = null;
		cur = db.rawQuery(sb.toString(),null);
		try { cur = db.rawQuery(sb.toString(),null); }
		catch (SQLiteException e) {
			if (cur != null) { cur.close(); }
			return null;
		}
		if (cur.moveToFirst()) {
			for (int x = 0; x < len; x++)
				settings[x] = cur.getString(x);
		} else {
			Log.w("T9DB.getSettings", "COULDN'T RETRIEVE SETTINGS?");
			for (int x = 1; x < len; x++) {
				settings[0] = "1"; // COLUMN_ID
				if (DBSettings.SETTING.settings[x].defvalue == null)
					settings[x] = null;
				else
					settings[x] = DBSettings.SETTING.settings[x].defvalue.toString();
			}
		}
		cur.close();
		return settings;
	}

	protected void addWord(String iword, LANGUAGE lang) throws DBException {
		Resources r = mContext.getResources();
		if (iword.equals("")) {
			throw new DBException(r.getString(R.string.add_word_blank));
		}
		// get int sequence
		String seq;
		try {
			seq = CharMap.getStringSequence(iword, lang);
		} catch (NullPointerException e) {
			throw new DBException(r.getString(R.string.add_word_badchar, lang.name(), iword));
		}
		// add int sequence into num table
		ContentValues values = new ContentValues();
		values.put(COLUMN_SEQ, seq);
		values.put(COLUMN_LANG, lang.id);
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
			String msg = r.getString(R.string.add_word_exist2, iword, lang.name());
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
		db.execSQL(UPDATEQ + id);
		// if id's freq is greater than FREQ_MAX, it gets normalized with trigger
	}

	protected void updateWords(String is, AbstractList<String> stringList, List<Integer> intList,
							int capsMode, LANGUAGE lang) {
		stringList.clear();
		intList.clear();
		// String[] sa = packInts(stringToInts(is), true);
		int islen = is.length();

		if (!checkReady()) {
			Log.e("T9DB.updateWords", "not ready");
			Toast.makeText(mContext, R.string.database_notready, Toast.LENGTH_SHORT).show();
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
				case CAPS_SINGLE:
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
			db.execSQL("CREATE INDEX IF NOT EXISTS idx ON " + WORD_TABLE_NAME + "("
					+ COLUMN_LANG + ", " + COLUMN_SEQ + " ASC, " + COLUMN_FREQUENCY + " DESC )");
			db.execSQL("CREATE TRIGGER IF NOT EXISTS " + FREQ_TRIGGER_NAME +
					" AFTER UPDATE ON " + WORD_TABLE_NAME +
					" WHEN NEW." + COLUMN_FREQUENCY + " > " + FREQ_MAX +
					" BEGIN" +
					" UPDATE " + WORD_TABLE_NAME + " SET " + COLUMN_FREQUENCY + " = "
					+ COLUMN_FREQUENCY + " / " + FREQ_DIV +
					" WHERE " + COLUMN_SEQ + " = NEW." + COLUMN_SEQ + ";" +
					" END;");

			createSettingsTable(db);

			StringBuilder sb = new StringBuilder("INSERT OR IGNORE INTO "); sb.append(SETTING_TABLE_NAME);
			sb.append(" ("); sb.append(COLUMN_ID); sb.append(", ");
			sb = DBSettings.SETTING.join(DBSettings.SETTING.settings, sb);
			sb.append(") VALUES (1,");
			for (int x=0;x<DBSettings.SETTING.settings.length; x++) {
				if (DBSettings.SETTING.settings[x].defvalue == null)
					sb.append("NULL");
				else
					sb.append(DBSettings.SETTING.settings[x].defvalue);
				if (x<DBSettings.SETTING.settings.length-1) sb.append(",");
			}
			sb.append(")");
			db.execSQL(sb.toString());
		}

		private void createSettingsTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + SETTING_TABLE_NAME + " (" +
					COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					DBSettings.SETTING.INPUT_MODE.id + " INTEGER, " +
					DBSettings.SETTING.LANG_SUPPORT.id + " INTEGER, " +
					DBSettings.SETTING.MODE_NOTIFY.id	+ " INTEGER, " +
					DBSettings.SETTING.LAST_LANG.id	+ " INTEGER, " +
					DBSettings.SETTING.KEY_REMAP.id	+ " INTEGER, " +
					DBSettings.SETTING.SPACE_ZERO.id	+ " INTEGER, " +
					DBSettings.SETTING.LAST_WORD.id	+ " TEXT )");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("T9DB.onUpgrade", "Upgrading database from version " + oldVersion + " to " + newVersion);
			if (oldVersion <= 1) {
				// ADDED LANG
				db.execSQL("DROP INDEX IF EXISTS idx");
				db.execSQL("ALTER TABLE " + WORD_TABLE_NAME + " ADD COLUMN " +
						COLUMN_LANG + " INTEGER");
				ContentValues updatedata = new ContentValues();
				updatedata.put(COLUMN_LANG, 0);
				db.update(WORD_TABLE_NAME, updatedata, null, null);
			}
			if (oldVersion <= 2) {
				// ADDED SETTINGS, CHANGED LANG VALUE
				db.execSQL("DROP INDEX IF EXISTS idx");
				db.execSQL("UPDATE " + WORD_TABLE_NAME + " SET " + COLUMN_LANG + "=" + LANGUAGE.RU.id +
						" WHERE " + COLUMN_LANG + "=1");
				db.execSQL("UPDATE " + WORD_TABLE_NAME + " SET " + COLUMN_LANG + "=" + LANGUAGE.EN.id +
						" WHERE " + COLUMN_LANG + "=0");
				createSettingsTable(db);
			}
			if (oldVersion == 3) {
				// ADDED REMAP OPTION and SPACEONZERO
				db.execSQL("ALTER TABLE " + SETTING_TABLE_NAME + " ADD COLUMN " +
					DBSettings.SETTING.KEY_REMAP.id + " INTEGER");
				db.execSQL("ALTER TABLE " + SETTING_TABLE_NAME + " ADD COLUMN " +
						DBSettings.SETTING.SPACE_ZERO.id + " INTEGER");
				ContentValues updatedata = new ContentValues();
				updatedata.put(DBSettings.SETTING.KEY_REMAP.id, 0);
				updatedata.put(DBSettings.SETTING.SPACE_ZERO.id, 0);
				db.update(SETTING_TABLE_NAME, updatedata, null, null);
			}
			onCreate(db);
			Log.i("T9DB.onUpgrade", "Done.");
		}
	}
}
