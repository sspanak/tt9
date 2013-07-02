package org.nyanya.android.traditionalt9;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class T9DB {

	protected static final String DATABASE_NAME = "t9dict.db";
	protected static final int DATABASE_VERSION = 1;
	protected static final String WORD_TABLE_NAME = "word";
	protected static final String FREQ_TRIGGER_NAME = "freqtrigger";
	//50k, 10k
	private static final int FREQ_MAX = 50000;
	private static final int FREQ_DIV = 10000;

	protected static final String COLUMN_ID = BaseColumns._ID;
	protected static final String COLUMN_SEQ = "seq";

	protected static final String COLUMN_WORD = "word";
	protected static final String COLUMN_FREQUENCY = "freq";

	private static final int MAX_RESULTS = 8;

	private static final int CAPS_OFF     = 0;
	private static final int CAPS_SINGLE  = 1;
	private static final int CAPS_ALL     = 2;
	
	private DatabaseHelper mOpenHelper;
	private SQLiteDatabase db;

	private Context parent;
	
	public T9DB(Context caller) {
		//create db
		parent = caller;
		mOpenHelper = new DatabaseHelper(caller);
	}
	
	public static SQLiteDatabase getSQLDB(Context caller) {
		return new DatabaseHelper(caller).getWritableDatabase();
	}
	
	public void init() {
		db = mOpenHelper.getWritableDatabase();
		//mOpenHelper.onUpgrade(db, 0, DATABASE_VERSION);
	}
	
	public void close() {
		db.close();
	}
	
	public void nuke() {
		init();
		mOpenHelper.onUpgrade(db, 0, DATABASE_VERSION);
		db.close();
		db = null;
	}

	public void addWord(String iword) throws DBException {
		Resources r = parent.getResources();
		if (iword.equals("")) {
			throw new DBException(r.getString(R.string.add_word_blank)); 
		}
		//get int sequence
		String seq = CharMap.getStringSequence(iword);
		//add int sequence into num table
		ContentValues values = new ContentValues();
		values.put(COLUMN_SEQ, seq);
		//add word into word
		values.put(COLUMN_WORD, iword);
		values.put(COLUMN_FREQUENCY, 1);
		try {
			db.insertOrThrow(WORD_TABLE_NAME, null, values);
		} catch (SQLiteConstraintException e) {
			String msg = r.getString(R.string.add_word_exist1) + iword + r.getString(R.string.add_word_exist2);
			Log.w("T9DB.addWord", msg);
			throw new DBException(msg);
		}
	}

	public void incrementWord(int id) {
		db.execSQL("UPDATE " + WORD_TABLE_NAME + " SET " + COLUMN_FREQUENCY + " = " +
			COLUMN_FREQUENCY + "+ 1 WHERE " + COLUMN_ID + " = \"" + id + "\"");
		// if id's freq is greater than X we should normalise those with the same seq
	}
	
	public void updateWords(String is, ArrayList<String> stringList, ArrayList<Integer> intList, int capsMode){
		stringList.clear();
		intList.clear();
		//String[] sa = packInts(stringToInts(is), true);
		int islen = is.length();
		String q = "SELECT " + COLUMN_ID + ", " + COLUMN_WORD + 
			" FROM " + WORD_TABLE_NAME + 
			" WHERE " + COLUMN_SEQ + "=?" +
			" ORDER BY " + COLUMN_FREQUENCY + " DESC";
		Cursor cur = db.rawQuery(q, new String[] {is});
		int hits = 0;
		for(cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			intList.add(cur.getInt(0));
			stringList.add(cur.getString(1));
			if (hits >= 15) {
				break;
			}
			hits++;
		}
		cur.close();
		if (hits < 4) {
			char c = is.charAt(islen-1);
			c++;
//			q = "SELECT " + COLUMN_WORD +", " + COLUMN_FREQUENCY + 
//					" FROM " + WORD_TABLE_NAME + 
//					" WHERE " + COLUMN_SEQ + " LIKE ?" +
//					" ORDER BY " + COLUMN_SEQ + " ASC, " + COLUMN_FREQUENCY + " DESC;";
//			c = db.rawQuery(q, new String[] {is + "_%"});
			//above is hella slow below is gotta query fast
			q = "SELECT " + COLUMN_ID + ", " + COLUMN_WORD + 
				" FROM " + WORD_TABLE_NAME + 
				" WHERE " + COLUMN_SEQ + " >= '" + is + "1" + "' AND " + COLUMN_SEQ + " < '" + is.substring(0, islen-1) + c + "'" + 
				" ORDER BY " + COLUMN_FREQUENCY + " DESC, " + COLUMN_SEQ + " ASC" +
				" LIMIT " + (MAX_RESULTS - hits);
			cur = db.rawQuery(q, null);
			
			for(cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				intList.add(cur.getInt(0));
				stringList.add(cur.getString(1));
				if (hits >= 10) {
					break;
				}
				hits++;
			}
			cur.close();
		}
		//Log.d("T9DB.updateWords", "pre: " + stringList);
		if (capsMode == CAPS_OFF) {
			return;
		}
		//Log.d("T9DB.updateWords", "filtering...");
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
				wordtemp = word.toUpperCase(Locale.US);
				if (wordtemp.equals(word)) {
					index++;
					continue;
				} else if (stringList.contains(wordtemp)){
					//remove this entry
					iter.remove();
					removed = true;
				} else {
					stringList.set(index, wordtemp);
				}
				break;	
			case CAPS_SINGLE:
				if (word.length() > 1) {
					wordtemp = word.substring(0, 1).toUpperCase(Locale.US) + word.substring(1);
				} else {
					wordtemp = word.toUpperCase(Locale.US);	
				}
				if (wordtemp.equals(word)) {
					index++;
					continue;
				} else if (stringList.contains(wordtemp)){
					//remove this entry
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
		return;
	}
	
	
	protected void updateWordsW(String is, ArrayList<String> stringList, ArrayList<Integer> intList,
			ArrayList<Integer> freq){
		stringList.clear();
		intList.clear();
		freq.clear();
		//String[] sa = packInts(stringToInts(is), true);
		int islen = is.length();
		String q = "SELECT " + COLUMN_ID + ", " + COLUMN_WORD + ", " + COLUMN_FREQUENCY + 
			" FROM " + WORD_TABLE_NAME + 
			" WHERE " + COLUMN_SEQ + "=?" +
			" ORDER BY " + COLUMN_FREQUENCY + " DESC";
		Cursor cur = db.rawQuery(q, new String[] {is});
		int hits = 0;
		for(cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			intList.add(cur.getInt(0));
			stringList.add(cur.getString(1));
			freq.add(cur.getInt(2));
			if (hits >= 10) {
				break;
			}
			hits++;
		}
		cur.close();
		if (hits < 4) {
			char c = is.charAt(islen-1);
			c++;
//			q = "SELECT " + COLUMN_WORD +", " + COLUMN_FREQUENCY + 
//					" FROM " + WORD_TABLE_NAME + 
//					" WHERE " + COLUMN_SEQ + " LIKE ?" +
//					" ORDER BY " + COLUMN_SEQ + " ASC, " + COLUMN_FREQUENCY + " DESC;";
//			c = db.rawQuery(q, new String[] {is + "_%"});
			//above is hella slow
			q = "SELECT " + COLUMN_ID + ", " + COLUMN_WORD + ", " + COLUMN_FREQUENCY +
				" FROM " + WORD_TABLE_NAME + 
				" WHERE " + COLUMN_SEQ + " >= '" + is + "' AND " + COLUMN_SEQ + " < '" + is.substring(0, islen-1) + c + "'" + 
				" ORDER BY " + COLUMN_FREQUENCY + " DESC, " + COLUMN_SEQ + " ASC" +
				" LIMIT " + (MAX_RESULTS - hits);
			cur = db.rawQuery(q, null);
			
			for(cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
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
		return;
	}
	

	protected static int[] stringToInts(String s){
		int[] il = new int[s.length()];
		for (int x = 0; x < s.length(); x++){
			il[x] = s.charAt(x) - 48; //lol hope this is ASCII all the time 
		}
		return il;
	}

	protected static String[] packInts(int[] intseq, boolean stringArray){
		int[] ia = packInts(intseq);
		String[] sa = {Integer.toString(ia[0]), Integer.toString(ia[1])};
		return sa;
	}
	protected static int[] packInts(int[] intseq){
		int[] ia = {0, 0};

		for (int x = 0; x < intseq.length; x++){
			if (x < 8){
				//packing 8 ints (<10) into 32bit int ... I hope...
				ia[0] = ia[0] | (intseq[x] << x*4);
			} else {
				ia[1] = intseq[x] << (x % 8) * 4;
			}
		}

		return ia;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + WORD_TABLE_NAME + " ("
					+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ COLUMN_SEQ + " TEXT, "
					+ COLUMN_WORD + " TEXT UNIQUE, "
					+ COLUMN_FREQUENCY + " INTEGER"
					+ ")");
			db.execSQL("CREATE INDEX idx ON " + WORD_TABLE_NAME + "("
					//+ COLUMN_NUMHIGH + ", " + COLUMN_NUMLOW + ")");
					+ COLUMN_SEQ + " ASC, " + COLUMN_FREQUENCY + " DESC )");
			db.execSQL("CREATE TRIGGER " + FREQ_TRIGGER_NAME + " AFTER UPDATE ON " + WORD_TABLE_NAME 
					+ " WHEN NEW." + COLUMN_FREQUENCY + " > " + FREQ_MAX
					+	" BEGIN" 
					+		" UPDATE " + WORD_TABLE_NAME + " SET " + COLUMN_FREQUENCY
					+			" = " + COLUMN_FREQUENCY + " / " + FREQ_DIV + " WHERE " + COLUMN_SEQ + " = NEW." + COLUMN_SEQ + ";" 
					+	" END;");
		}
			
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("T9DB", "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + WORD_TABLE_NAME);
			db.execSQL("DROP INDEX IF EXISTS idx");
			onCreate(db);
			Log.w("T9DB", "Done.");
		}
	}
}
