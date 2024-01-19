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


	private String getPositionsQuery(Language language, String sequence, boolean isFilterOn) {
		int generations;
		if (sequence.length() == 2 && !isFilterOn) {
			generations = 1;
		} else if (sequence.length() == 2 || (sequence.length() <= 4 && !isFilterOn)) {
			generations = 2;
		} else {
			generations = Integer.MAX_VALUE;
		}

		return getPositionsQuery(language, sequence, generations);
	}


	private String getPositionsQuery(Language language, String sequence, int generations) {
		if (sequence.length() < 2) {
			return "";
		}

		// @todo: use a compiled version if Language has not changed since the the last time
		StringBuilder sql = new StringBuilder("SELECT `start`, `end` FROM " + dbBox.getWordPositionsTable(language.getId()) + " WHERE ");

		if (generations >= 0 && generations < 10) {
			sql.append(" sequence IN(").append(sequence);

			int lastChild = (int)Math.pow(10, generations) - 1;

			for (int seqEnd = 1; seqEnd <= lastChild; seqEnd++) {
				if (seqEnd % 10 != 0) {
					sql.append(",").append(sequence).append(seqEnd);
				}
			}

			sql.append(")");
		} else {
			sql.append(" sequence = ").append(sequence).append(" OR sequence BETWEEN ").append(sequence).append("1 AND ").append(sequence).append("9");
			sql.append(" ORDER BY start ");
			sql.append(" LIMIT 100"); // @todo: maximum number of children + 1 among all sequences
		}

		String positionsSql = sql.toString();
		Logger.d(LOG_TAG, "Index SQL: " + positionsSql);
		return positionsSql;
	}


	private String getWordsQuery(Language language, String positions, String filter, int maximumWords) {
		// @todo: use a compiled version if Language has not changed since the the last time
		// @todo: UNION with the custom words table

		StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT word FROM ").append(dbBox.getWordsTable(language.getId()))
			.append(" WHERE position IN(").append(positions).append(")");

		if (!filter.isEmpty()) {
			sql.append(" AND word LIKE '").append(filter).append("%'");
		}

		sql
			.append(" ORDER BY LENGTH(word), frequency DESC")
			.append(" LIMIT ").append(maximumWords);

		String wordsSql = sql.toString();
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


	public ArrayList<String> getSimilar(Language language, @NonNull String sequence, @NonNull String filter, int minWords, int maxWords) {
		if (db == null) {
			Logger.e(LOG_TAG, "No database connection. Cannot query any data.");
			return new ArrayList<>();
		}

		long startTime = System.currentTimeMillis();
		String positions = getWordPositions(language, sequence, !filter.isEmpty(), minWords);
		long positionsTime = System.currentTimeMillis() - startTime;


		startTime = System.currentTimeMillis();
		ArrayList<String> words = getWords(language, positions, filter, maxWords);
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


	private String getWordPositions(Language language, String sequence, boolean isFilterOn, int minPositions) {
		if (sequence.length() == 1) {
			return sequence;
		}

		String sql = getPositionsQuery(language, sequence, isFilterOn);
		if (sql.isEmpty()) {
			return "";
		}

		Cursor cursor = db.rawQuery(sql, new String[]{});
		WordPositions positions = WordPositions.fromDbRanges(cursor);
		cursor.close();

		if (positions.size < minPositions) {
			Logger.d(LOG_TAG, "Not enough positions: " + positions.size + " < " + minPositions + ". Searching for more.");
			sql = getPositionsQuery(language, sequence, Integer.MAX_VALUE);
			cursor = db.rawQuery(sql, new String[]{});
			positions = WordPositions.fromDbRanges(cursor);
			cursor.close();
		}

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

		StringBuilder sql = new StringBuilder("INSERT INTO " + dbBox.getWordPositionsTable(language.getId()) + " (sequence, start, end) VALUES ");

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
			.append(" ms (positions: ").append(positionIndexTime)
			.append(" ms, words: ").append(wordsTime).append(" ms).");

		if (words.isEmpty()) {
			debugText.append(" Sequence: ").append(sequence);
		} else {
			debugText.append("\n").append(words);
		}

		Logger.d(LOG_TAG, debugText.toString());
	}
}
