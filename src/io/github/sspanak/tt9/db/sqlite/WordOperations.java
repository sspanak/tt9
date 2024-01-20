package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

class WordOperations {
	private final String LOG_TAG = "sqlite.WordOperations";

	private static final String CUSTOM_WORDS_TABLE = "custom_words_";
	private static final String POSITIONS_TABLE_BASE_NAME = "word_positions_";
	private static final String WORDS_TABLE_BASE_NAME = "words_";


	static String getWordsTable(int langId) { return WORDS_TABLE_BASE_NAME + langId; }

	static String getWordPositionsTable(int langId) { return POSITIONS_TABLE_BASE_NAME + langId; }


	/**
	 * INIT
	 */


	static String[] getCreateTableQueries(Context context) {
		int languageCount = LanguageCollection.count(context);
		String[] queries = new String[languageCount * 4 + 2];

		queries[0] = getCustomWordsTableQuery();
		queries[1] = getCustomWordsIndexQuery();

		int queryId = 2;
		for (Language language : LanguageCollection.getAll(context)) {
			queries[queryId++] = getWordsTableQuery(language.getId());
			queries[queryId++] = createWordsIndexQuery(language.getId());
			queries[queryId++] = getWordPositionsTableQuery(language.getId());
			queries[queryId++] = createWordsPositionsIndexQuery(language.getId());
		}

		return queries;
	}

	private static String getWordsTableQuery(int langId) {
		return
			"CREATE TABLE IF NOT EXISTS " + getWordsTable(langId) + " (" +
				"frequency INTEGER NOT NULL DEFAULT 0, " +
				"isCustom TINYINT NOT NULL DEFAULT 0, " +
				"position INTEGER NOT NULL, " +
				"word TEXT NOT NULL" +
			")";
	}

	private static String createWordsIndexQuery(int langId) {
		return "CREATE INDEX IF NOT EXISTS idx_position_" + langId + " ON " + getWordsTable(langId) + " (position, word)";
	}

	private static String getWordPositionsTableQuery(int langId) {
		return
			"CREATE TABLE IF NOT EXISTS " + getWordPositionsTable(langId) + " (" +
				"sequence TEXT NOT NULL, " +
				"start INTEGER NOT NULL, " +
				"end INTEGER NOT NULL" +
			")";
	}

	private static String createWordsPositionsIndexQuery(int langId) {
		return "CREATE INDEX IF NOT EXISTS idx_sequence_start_" + langId + " ON " + getWordPositionsTable(langId) + " (sequence, `start`)";
	}

	static private String getCustomWordsTableQuery() {
		return "CREATE TABLE IF NOT EXISTS " + CUSTOM_WORDS_TABLE + " (" +
			"id INTEGER PRIMARY KEY, " +
			"langId INTEGER NOT NULL, " +
			"sequence TEXT NOT NULL, " +
			"word INTEGER NOT NULL " +
		")";
	}

	static private String getCustomWordsIndexQuery() {
		return "CREATE INDEX IF NOT EXISTS idx_langId_sequence ON " + CUSTOM_WORDS_TABLE + " (langId, sequence)";
	}


	/**
	 * CREATE
	 */


	void insertWords(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull ArrayList<Word> words) {
		if (words.size() == 0) {
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO " + getWordsTable(language.getId()) + " (frequency, position, word) VALUES ");
		for (Word word : words) {
			sql
				.append("(")
				.append(word.frequency).append(",").append(word.position).append(",'").append(word.word.replaceAll("'", "''")).append("'")
				.append("),");
		}
		sql.setLength(sql.length() - 1);

		db.execSQL(sql.toString());
	}


	void insertPositions(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull ArrayList<WordPosition> positions) {
		if (positions.size() == 0) {
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO " + getWordPositionsTable(language.getId()) + " (sequence, start, end) VALUES ");

		for (WordPosition pos : positions) {
			sql
				.append("(")
				.append(pos.sequence).append(",").append(pos.start).append(",").append(pos.end)
				.append("),");
		}
		sql.setLength(sql.length() - 1);

		db.execSQL(sql.toString());
	}


	/**
	 * READ
	 */


	@NonNull ArrayList<String> getWords(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String positions, String filter, int maximumWords) {
		if (positions.isEmpty()) {
			Logger.i(LOG_TAG, "No word positions. Not searching words.");
			return new ArrayList<>();
		}

		ArrayList<String> words = new ArrayList<>();

		String wordsQuery = getWordsQuery(language, positions, filter, maximumWords);
		if (wordsQuery.isEmpty()) {
			return words;
		}

		try (Cursor cursor = db.rawQuery(wordsQuery, null)) {
			while (cursor.moveToNext()) {
				words.add(cursor.getString(0));
			}
		}

		return words;
	}


	@NonNull String getWordPositions(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String sequence, boolean isFilterOn, int minPositions) {
		if (sequence.length() == 1) {
			return sequence;
		}

		String sql = getPositionsQuery(language, sequence, isFilterOn);
		if (sql.isEmpty()) {
			return "";
		}

		Cursor cursor = db.rawQuery(sql, new String[]{});
		WordPositionsStringBuilder positions = WordPositionsStringBuilder.fromDbRanges(cursor);
		cursor.close();

		if (positions.size < minPositions) {
			Logger.d(LOG_TAG, "Not enough positions: " + positions.size + " < " + minPositions + ". Searching for more.");

			sql = getPositionsQuery(language, sequence, Integer.MAX_VALUE);
			cursor = db.rawQuery(sql, new String[]{});
			positions = WordPositionsStringBuilder.fromDbRanges(cursor);
			cursor.close();
		}

		return positions.toString();
	}


	@NonNull private String getPositionsQuery(@NonNull Language language, @NonNull String sequence, boolean isFilterOn) {
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


	@NonNull private String getPositionsQuery(@NonNull Language language, @NonNull String sequence, int generations) {
		if (sequence.length() < 2) {
			return "";
		}

		// @todo: use a compiled version if Language has not changed since the the last time
		StringBuilder sql = new StringBuilder("SELECT `start`, `end` FROM ")
			.append(getWordPositionsTable(language.getId()))
			.append(" WHERE ");

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


	@NonNull private String getWordsQuery(@NonNull Language language, @NonNull String positions, @NonNull String filter, int maximumWords) {
		// @todo: use a compiled version if Language has not changed since the the last time
		// @todo: UNION with the custom words table

		StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT word FROM ").append(getWordsTable(language.getId()))
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
}
