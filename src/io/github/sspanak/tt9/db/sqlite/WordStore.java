package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;

public class WordStore {
	private final String LOG_TAG = "sqlite.WordStore";
	private SQLiteBox dbBox = null;
	private SQLiteDatabase db = null;


	public WordStore(Context context) {
		try {
			dbBox = new SQLiteBox(context);
			db = dbBox.getWritableDatabase();
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Database connection failure. All operations will return empty results. " + e.getMessage());
		}
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


	public void put(@NonNull Language language, @NonNull DictionaryWordBatch wordBatch) {
		if (db == null) {
			Logger.e(LOG_TAG, "No database connection. Cannot put any data.");
			return;
		}
		insertWords(language, wordBatch.words);
		insertSequences(language, wordBatch.sequences);
	}


	public ArrayList<String> getMany(Language language, String sequence, String filter, int maximumWords) {
		if (db == null) {
			Logger.e(LOG_TAG, "No database connection. Cannot query any data.");
			return new ArrayList<>();
		}

		boolean isFilterOn = filter != null && !filter.isEmpty();

		long startTime = System.currentTimeMillis();

		String indexSql = "SELECT `start`, `end` FROM " + dbBox.getIndexTable(language.getId()) + " WHERE ";
		if (sequence.length() == 1) { // @todo: no sequences query here, just search for words
			indexSql += " sequence = " + sequence;
		} else if (sequence.length() == 2 && !isFilterOn) {
			String[] children = new String[10];
			children[0] = sequence;
			for (int i = 1; i <= 9; i++) {
				children[i] = sequence + i;
			}
			indexSql += " sequence IN(" + String.join(",", children) + ")";
		} else if (sequence.length() == 2 || (sequence.length() <= 4 && !isFilterOn)) {
			String[] children = new String[91];
			children[0] = sequence;
			for (int i = 1; i <= 9; i++) {
				children[i] = sequence + i;
			}

			for (int seqEnd = 11, j = 10; seqEnd <= 99; seqEnd++) {
				if (seqEnd % 10 != 0) {
					children[j++] = sequence + seqEnd;
				}
			}
			indexSql += " sequence IN(" + String.join(",", children) + ")";
		} else {
			indexSql += " sequence = " + sequence + " OR sequence BETWEEN " + sequence + "1 AND " + sequence + "9";
			indexSql += " ORDER BY start ";
			indexSql += " LIMIT 100"; // @todo: maximum number of children + 1 among all sequences
		}

		// @todo: if results for 2-4 < SettingsStore.getSuggestionsMin(), run the >= 4 query

		Logger.d("WordStore", "Index SQL: " + indexSql);

		Cursor cursor = db.rawQuery(indexSql, new String[]{});

		StringBuilder positions = new StringBuilder();

		while (cursor.moveToNext()) {
			int start = cursor.getInt(0);
			int end = cursor.getInt(1);

			if (start < end) {
				for (int position = start; position <= end; position++) {
					positions.append(position);
					if (position < end) {
						positions.append(",");
					}
				}
			} else {
				positions.append(start);
			}

			positions.append(",");
		}

		cursor.close();

		Logger.d("WordStore", "Index time: " + (System.currentTimeMillis() - startTime) + " ms.");

		if (positions.length() == 0) {
			Logger.d("WordStore", "No sequences found. Not searching words.");
			return new ArrayList<>();
		}

		positions.setLength(positions.length() - 1);

		startTime = System.currentTimeMillis();

		String wordsSql =
			"SELECT word " +
			" FROM " + dbBox.getWordsTable(language.getId()) +
			" WHERE position IN(" + positions + ")" + (isFilterOn ? " AND word LIKE '" + filter + "%' " : "") +
			" ORDER BY LENGTH(word), frequency DESC " +
			" LIMIT " + maximumWords;
		Logger.d("WordStore", "Words SQL: " + wordsSql);


		ArrayList<String> words = new ArrayList<>();
		cursor = db.rawQuery(wordsSql, new String[]{});
		while (cursor.moveToNext()) {
			words.add(cursor.getString(0));
		}

		cursor.close();

		Logger.d("WordStore", "Words time: " + (System.currentTimeMillis() - startTime) + " ms.");

		return words;
	}


	private void insertWords(Language language, ArrayList<Word> words) {
		if (words.size() == 0) {
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO " + dbBox.getWordsTable(language.getId()) + " (frequency, position, word) VALUES ");
		for (Word word : words) {
			sql
				.append("(")
				.append(word.frequency).append(",").append(word.position).append(",'").append(word.word.replaceAll("'", "''")).append("'")
				.append("),");
		}
		sql.setLength(sql.length() - 1);

		db.execSQL(sql.toString());
	}


	private void insertSequences(Language language, ArrayList<SequenceRange> sequences) {
		if (sequences.size() == 0) {
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO " + dbBox.getIndexTable(language.getId()) + " (sequence, start, end) VALUES ");

		for (SequenceRange seq : sequences) {
			sql
				.append("(")
				.append(seq.sequence).append(",").append(seq.start).append(",").append(seq.end)
				.append("),");
		}
		sql.setLength(sql.length() - 1);

		db.execSQL(sql.toString());
	}
}
