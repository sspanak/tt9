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


	private String getSequencesQuery(Language language, String sequence, boolean isFilterOn) {
		if (sequence.length() < 2) {
			return "";
		}

		String sql = "SELECT `start`, `end` FROM " + dbBox.getIndexTable(language.getId()) + " WHERE ";

		String[] children;

		if (sequence.length() == 2 && !isFilterOn) {
			children = new String[10];
			children[0] = sequence;
			for (int i = 1; i <= 9; i++) {
				children[i] = sequence + i;
			}

			sql += " sequence IN(" + String.join(",", children) + ")";
		} else if (sequence.length() == 2 || (sequence.length() <= 4 && !isFilterOn)) {
			children = new String[91];
			children[0] = sequence;

			for (int seqEnd = 1, j = 1; seqEnd <= 99; seqEnd++) {
				if (seqEnd % 10 != 0) {
					children[j++] = sequence + seqEnd;
				}
			}

			sql += " sequence IN(" + String.join(",", children) + ")";
		} else {
			sql += " sequence = " + sequence + " OR sequence BETWEEN " + sequence + "1 AND " + sequence + "9";
			sql += " ORDER BY start ";
			sql += " LIMIT 100"; // @todo: maximum number of children + 1 among all sequences
		}

		Logger.d(LOG_TAG, "Index SQL: " + sql);
		return sql;
	}


	private String getWordsQuery(Language language, String positions, String filter, int maximumWords) {
		// @todo: use a compiled version if Language is the same as the last time
		String wordsSql =
			"SELECT word " +
			" FROM " + dbBox.getWordsTable(language.getId()) +
			" WHERE position IN(" + positions + ")" + (filter.isEmpty() ? "" : " AND word LIKE '" + filter + "%' ") +
			" ORDER BY LENGTH(word), frequency DESC " +
			" LIMIT " + maximumWords;

		Logger.d(LOG_TAG, "Words SQL: " + wordsSql);
		return wordsSql;
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


	public ArrayList<String> getMany(Language language, @NonNull String sequence, @NonNull String filter, int maximumWords) {
		if (db == null) {
			Logger.e(LOG_TAG, "No database connection. Cannot query any data.");
			return new ArrayList<>();
		}

		long startTime = System.currentTimeMillis();
		String positions =  getSequencePositions(language, sequence, !filter.isEmpty());
		long positionsTime = System.currentTimeMillis() - startTime;


		startTime = System.currentTimeMillis();
		ArrayList<String> words = getWords(language, positions, filter, maximumWords);
		long wordsTime = System.currentTimeMillis() - startTime;

		printLoadingSummary(sequence, words, positionsTime, wordsTime);

		return words;
	}


	private ArrayList<String> getWords(Language language, String positions, String filter, int maximumWords) {
		if (positions.length() == 0) {
			Logger.i(LOG_TAG, "No word positions. Not searching words.");
			return new ArrayList<>();
		}

		ArrayList<String> words = new ArrayList<>();

		Cursor cursor = db.rawQuery(
			getWordsQuery(language, positions, filter, maximumWords),
			null
		);
		while (cursor.moveToNext()) {
			words.add(cursor.getString(0));
		}
		cursor.close();

		return words;
	}


	private String getSequencePositions(Language language, String sequence, boolean isFilterOn) {
		if (sequence.length() == 1) {
			return sequence;
		}

		String sql = getSequencesQuery(language, sequence, isFilterOn);
		if (sql.isEmpty()) {
			return sql;
		}

		Cursor cursor = db.rawQuery(sql, new String[]{});
		String positions = dbRangesToPositions(cursor);
		cursor.close();

		// @todo: if (sequence.length() >= 2 && sequence.length() <= 4 && [positions count] < SettingsStore.getSuggestionsMin()) { run the >= 4 query }

		return positions;
	}

	public static String dbRangesToPositions(Cursor cursor) {
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

		positions.setLength(Math.max(0, positions.length() - 1));

		return positions.toString();
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


	private void printLoadingSummary(String sequence, ArrayList<String> words, long positionIndexTime, long wordsTime) {
		if (!Logger.isDebugLevel()) {
			return;
		}

		StringBuilder debugText = new StringBuilder("===== Word Loading Summary =====");
		debugText
			.append("\nWord Count: ").append(words.size())
			.append(".\nTime: ").append(positionIndexTime + wordsTime)
			.append(" ms (index: ").append(wordsTime)
			.append(" ms, words: ").append(positionIndexTime + wordsTime).append(" ms).");

		if (words.isEmpty()) {
			debugText.append(" Sequence: ").append(sequence);
		} else {
			debugText.append("\n").append(words);
		}

		Logger.d(LOG_TAG, debugText.toString());
	}
}
