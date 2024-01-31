package io.github.sspanak.tt9.db.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.SlowQueryStats;
import io.github.sspanak.tt9.db.entities.WordList;
import io.github.sspanak.tt9.db.entities.WordPositionsStringBuilder;
import io.github.sspanak.tt9.languages.Language;

public class ReadOps {
	private final String LOG_TAG = "ReadOperations";


	/**
	 * Checks if a word exists in the database for the given language (case-insensitive).
	 */
	public boolean exists(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String word) {
		String lowercaseWord = word.toLowerCase(language.getLocale());
		String uppercaseWord = word.toUpperCase(language.getLocale());

		SQLiteStatement query = CompiledQueryCache.get(db, "SELECT COUNT(*) FROM " + Tables.getWords(language.getId()) + " WHERE word IN(?, ?, ?)");
		query.bindString(1, word);
		query.bindString(2, lowercaseWord);
		query.bindString(3, uppercaseWord);
		try {
			return query.simpleQueryForLong() > 0;
		} catch (SQLiteDoneException e) {
			return false;
		}
	}


	/**
	 * Checks if language exists (has words) in the database.
	 */
	public boolean exists(@NonNull SQLiteDatabase db, int langId) {
		return CompiledQueryCache.simpleQueryForLong(
			db,
			"SELECT COUNT(*) FROM " + Tables.getWords(langId),
			0
		) > 0;
	}


	@NonNull
	public WordList getWords(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String positions, String filter, int maximumWords, boolean fullOutput) {
		if (positions.isEmpty()) {
			Logger.d(LOG_TAG, "No word positions. Not searching words.");
			return new WordList();
		}


		String wordsQuery = getWordsQuery(language, positions, filter, maximumWords, fullOutput);
		if (wordsQuery.isEmpty()) {
			return new WordList();
		}

		WordList words = new WordList();
		try (Cursor cursor = db.rawQuery(wordsQuery, null)) {
			while (cursor.moveToNext()) {
					words.add(
						cursor.getString(0),
						fullOutput ? cursor.getInt(1) : 0,
						fullOutput ? cursor.getInt(2) : 0
					);
			}
		}

		return words;
	}


	public String getSimilarWordPositions(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String sequence, String wordFilter, int minPositions) {
		int generations;

		switch (sequence.length()) {
			case 2:
				generations = wordFilter.isEmpty() ? 1 : Integer.MAX_VALUE;
				break;
			case 3:
			case 4:
				generations = wordFilter.isEmpty() ? 2 : Integer.MAX_VALUE;
				break;
			default:
				generations = Integer.MAX_VALUE;
				break;
		}

		return getWordPositions(db, language, sequence, generations, minPositions, wordFilter);
	}


	@NonNull
	public String getWordPositions(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String sequence, int generations, int minPositions, String wordFilter) {
		if (sequence.length() == 1) {
			return sequence;
		}

		WordPositionsStringBuilder positions = new WordPositionsStringBuilder();

		String cachedFactoryPositions = SlowQueryStats.getCachedIfSlow(SlowQueryStats.generateKey(language, sequence, wordFilter, minPositions));
		if (cachedFactoryPositions != null) {
			String customWordPositions = getCustomWordPositions(db, language, sequence, generations);
			return customWordPositions.isEmpty() ? cachedFactoryPositions : customWordPositions + "," + cachedFactoryPositions;
		}

		try (Cursor cursor = db.rawQuery(getPositionsQuery(language, sequence, generations), null)) {
			positions.appendFromDbRanges(cursor);
		}

		if (positions.size < minPositions) {
			Logger.d(LOG_TAG, "Not enough positions: " + positions.size + " < " + minPositions + ". Searching for more.");
			try (Cursor cursor = db.rawQuery(getFactoryWordPositionsQuery(language, sequence, Integer.MAX_VALUE), null)) {
				positions.appendFromDbRanges(cursor);
			}
		}

		return positions.toString();
	}



	@NonNull private String getCustomWordPositions(@NonNull SQLiteDatabase db, Language language, String sequence, int generations) {
		try (Cursor cursor = db.rawQuery(getCustomWordPositionsQuery(language, sequence, generations), null)) {
			return new WordPositionsStringBuilder().appendFromDbRanges(cursor).toString();
		}
	}


	private String getPositionsQuery(@NonNull Language language, @NonNull String sequence, int generations) {
		return
			"SELECT `start`, `end` FROM ( " +
				getFactoryWordPositionsQuery(language, sequence, generations) +
				") UNION " +
				getCustomWordPositionsQuery(language, sequence, generations);
	}



	@NonNull private String getFactoryWordPositionsQuery(@NonNull Language language, @NonNull String sequence, int generations) {
		StringBuilder sql = new StringBuilder("SELECT `start`, `end` FROM ")
			.append(Tables.getWordPositions(language.getId()))
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
			sql.append(" ORDER BY `start` ");
			sql.append(" LIMIT 100");
		}

		String positionsSql = sql.toString();
		Logger.v(LOG_TAG, "Index SQL: " + positionsSql);
		return positionsSql;
	}


	@NonNull private String getCustomWordPositionsQuery(@NonNull Language language, @NonNull String sequence, int generations) {
		String sql = "SELECT -id as `start`, -id as `end` FROM " + Tables.CUSTOM_WORDS +
			" WHERE langId = " + language.getId() +
			" AND (sequence = " + sequence;

		if (generations > 0) {
			sql += " OR sequence BETWEEN " + sequence + "1 AND " + sequence + "9)";
		} else {
			sql += ")";
		}

		Logger.v(LOG_TAG, "Custom words SQL: " + sql);
		return sql;
	}


	@NonNull private String getWordsQuery(@NonNull Language language, @NonNull String positions, @NonNull String filter, int maxWords, boolean fullOutput) {
		StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT word");
		if (fullOutput) {
			sql.append(",frequency,position");
		}

		sql.append(" FROM ").append(Tables.getWords(language.getId()))
			.append(" WHERE position IN(").append(positions).append(")");

		if (!filter.isEmpty()) {
			sql.append(" AND word LIKE '").append(filter.replaceAll("'", "''")).append("%'");
		}

		sql
			.append(" ORDER BY LENGTH(word), frequency DESC")
			.append(" LIMIT ").append(maxWords);

		String wordsSql = sql.toString();
		Logger.v(LOG_TAG, "Words SQL: " + wordsSql);
		return wordsSql;
	}


	public int getNextInNormalizationQueue(@NonNull SQLiteDatabase db) {
		return (int) CompiledQueryCache.simpleQueryForLong(
			db,
			"SELECT langId FROM " + Tables.LANGUAGES_META + " WHERE normalizationPending = 1 LIMIT 1",
			-1
		);
	}
}
