package io.github.sspanak.tt9.db.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;

public class ReadOperations {
	private final String LOG_TAG = "ReadOperations";


	public boolean exists(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String word) {
		// @todo: use a compiled query
		String sql = "SELECT COUNT(*) FROM " + TableOperations.getWordsTable(language.getId()) + " WHERE word = ?";
		try (Cursor cursor = db.rawQuery(sql, new String[]{word})) {
			cursor.moveToFirst();
			return cursor.getInt(0) > 0;
		}
	}


	public boolean exists(@NonNull SQLiteDatabase db, @NonNull Language language) {
		String sql = "SELECT COUNT(*) FROM " + TableOperations.getWordsTable(language.getId());
		try (Cursor cursor = db.rawQuery(sql, null)) {
			cursor.moveToFirst();
			return cursor.getInt(0) > 0;
		}
	}


	@NonNull
	public ArrayList<String> getWords(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String positions, String filter, int maximumWords) {
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


	@NonNull
	public String getWordPositions(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String sequence, boolean isFilterOn, int minPositions) {
		if (sequence.length() == 1) {
			return sequence;
		}

		WordPositionsStringBuilder positions = new WordPositionsStringBuilder();

		try (Cursor cursor = db.rawQuery(getPositionsQuery(language, sequence, isFilterOn), new String[]{})) {
			positions.appendFromDbRanges(cursor);
		}

		if (positions.size < minPositions) {
			Logger.d(LOG_TAG, "Not enough positions: " + positions.size + " < " + minPositions + ". Searching for more.");
			try (Cursor cursor = db.rawQuery(getFactoryWordPositionsQuery(language, sequence, Integer.MAX_VALUE), new String[]{})) {
				positions.appendFromDbRanges(cursor);
			}
		}

		return positions.toString();
	}

	@NonNull private String getPositionsQuery(@NonNull Language language, @NonNull String sequence, boolean isFilterOn) {
		return
			"SELECT `start`, `end` FROM ( " +
				getFactoryWordPositionsQuery(language, sequence, isFilterOn) +
				") UNION " +
				getCustomWordPositionsQuery(language, sequence);
	}


	@NonNull
	private String getFactoryWordPositionsQuery(@NonNull Language language, @NonNull String sequence, boolean isFilterOn) {
		int generations;

		switch (sequence.length()) {
			case 2:
				generations = isFilterOn ? Integer.MAX_VALUE : 1;
				break;
			case 3:
			case 4:
				generations = isFilterOn ? Integer.MAX_VALUE : 2;
				break;
			default:
				generations = Integer.MAX_VALUE;
				break;
		}

		return getFactoryWordPositionsQuery(language, sequence, generations);
	}



	@NonNull private String getFactoryWordPositionsQuery(@NonNull Language language, @NonNull String sequence, int generations) {
		// @todo: use a compiled query
		StringBuilder sql = new StringBuilder("SELECT `start`, `end` FROM ")
			.append(TableOperations.getWordPositionsTable(language.getId()))
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


	@NonNull private String getCustomWordPositionsQuery(@NonNull Language language, @NonNull String sequence) {
		// @todo: use a compiled query
		String sql = "SELECT -id as `start`, -id as `end` FROM " + TableOperations.CUSTOM_WORDS_TABLE +
			" WHERE langId = " + language.getId() +
			" AND (sequence = " + sequence + " OR sequence BETWEEN " + sequence + "1 AND " + sequence + "9)";

		Logger.d(LOG_TAG, "Custom words SQL: " + sql);
		return sql;
	}


	@NonNull private String getWordsQuery(@NonNull Language language, @NonNull String positions, @NonNull String filter, int maximumWords) {
		// @todo: use a compiled query

		StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT word FROM ").append(TableOperations.getWordsTable(language.getId()))
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
