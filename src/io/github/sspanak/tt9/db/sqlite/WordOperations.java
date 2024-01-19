package io.github.sspanak.tt9.db.sqlite;

import android.database.Cursor;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;

class WordOperations {
	private final String LOG_TAG = "sqlite.WordOperations";
	private final SQLiteStore sqlite;

	WordOperations(SQLiteStore sqlite) {
		this.sqlite = sqlite;
	}


	/**
	 * CREATE
	 */

	void insertWords(@NonNull Language language, @NonNull ArrayList<Word> words) {
		if (words.size() == 0) {
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO " + SQLiteStore.getWordsTable(language.getId()) + " (frequency, position, word) VALUES ");
		for (Word word : words) {
			sql
				.append("(")
				.append(word.frequency).append(",").append(word.position).append(",'").append(word.word.replaceAll("'", "''")).append("'")
				.append("),");
		}
		sql.setLength(sql.length() - 1);

		sqlite.getDb().execSQL(sql.toString());
	}


	void insertPositions(@NonNull Language language, @NonNull ArrayList<WordPosition> positions) {
		if (positions.size() == 0) {
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO " + SQLiteStore.getWordPositionsTable(language.getId()) + " (sequence, start, end) VALUES ");

		for (WordPosition pos : positions) {
			sql
				.append("(")
				.append(pos.sequence).append(",").append(pos.start).append(",").append(pos.end)
				.append("),");
		}
		sql.setLength(sql.length() - 1);

		sqlite.getDb().execSQL(sql.toString());
	}


	/**
	 * READ
	 */


	@NonNull ArrayList<String> getWords(@NonNull Language language, @NonNull String positions, String filter, int maximumWords) {
		if (positions.isEmpty()) {
			Logger.i(LOG_TAG, "No word positions. Not searching words.");
			return new ArrayList<>();
		}

		ArrayList<String> words = new ArrayList<>();

		String wordsQuery = getWordsQuery(language, positions, filter, maximumWords);
		if (wordsQuery.isEmpty()) {
			return words;
		}

		try (Cursor cursor = sqlite.getDb().rawQuery(wordsQuery, null)) {
			while (cursor.moveToNext()) {
				words.add(cursor.getString(0));
			}
		}

		return words;
	}


	@NonNull String getWordPositions(@NonNull Language language, @NonNull String sequence, boolean isFilterOn, int minPositions) {
		if (sequence.length() == 1) {
			return sequence;
		}

		String sql = getPositionsQuery(language, sequence, isFilterOn);
		if (sql.isEmpty()) {
			return "";
		}

		Cursor cursor = sqlite.getDb().rawQuery(sql, new String[]{});
		WordPositionsStringBuilder positions = WordPositionsStringBuilder.fromDbRanges(cursor);
		cursor.close();

		if (positions.size < minPositions) {
			Logger.d(LOG_TAG, "Not enough positions: " + positions.size + " < " + minPositions + ". Searching for more.");

			sql = getPositionsQuery(language, sequence, Integer.MAX_VALUE);
			cursor = sqlite.getDb().rawQuery(sql, new String[]{});
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
			.append(SQLiteStore.getWordPositionsTable(language.getId()))
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
			.append("SELECT word FROM ").append(SQLiteStore.getWordsTable(language.getId()))
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


	/**
	 * DELETE
	 */

	void removeMany(@NonNull ArrayList<Integer> languageIds) {
		for (int langId : languageIds) {
			sqlite.getDb().execSQL("DELETE FROM " + SQLiteStore.getWordsTable(langId));
			sqlite.getDb().execSQL("DELETE FROM " + SQLiteStore.getWordPositionsTable(langId));
		}
	}
}
