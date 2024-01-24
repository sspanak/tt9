package io.github.sspanak.tt9.db.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.entities.WordList;
import io.github.sspanak.tt9.db.entities.WordPositionsStringBuilder;
import io.github.sspanak.tt9.languages.Language;

public class ReadOperations {
	private final String LOG_TAG = "ReadOperations";

	private static final HashMap<String, SQLiteStatement> statements = new HashMap<>();


	public boolean exists(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String word) {
		String key = "exists_" + language.getId() + "_" + word;
		if (!statements.containsKey(key)) {
			statements.put(key, db.compileStatement("SELECT COUNT(*) FROM " + TableOperations.getWordsTable(language.getId()) + " WHERE word = ?"));
		}

		SQLiteStatement query = statements.get(key);
		if (query != null) {
			query.bindString(1, word);
			try {
				return query.simpleQueryForLong() > 0;
			} catch (SQLiteDoneException e) {
				return false;
			}
		}

		return false;
	}


	public boolean exists(@NonNull SQLiteDatabase db, int langId) {
		String key = "exists_" + langId;
		if (!statements.containsKey(key)) {
			statements.put(key, db.compileStatement("SELECT COUNT(*) FROM " + TableOperations.getWordsTable(langId)));
		}

		SQLiteStatement query = statements.get(key);
		try {
			return query != null && query.simpleQueryForLong() > 0;
		} catch (SQLiteDoneException e) {
			return false;
		}
	}


	@NonNull
	public WordList getWords(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String positions, String filter, int maximumWords, boolean fullOutput) {
		if (positions.isEmpty()) {
			Logger.i(LOG_TAG, "No word positions. Not searching words.");
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


	public String getSimilarWordPositions(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String sequence, boolean isFilterOn, int minPositions) {
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

		return getWordPositions(db, language, sequence, generations, minPositions);
	}


	@NonNull
	public String getWordPositions(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String sequence, int generations, int minPositions) {
		if (sequence.length() == 1) {
			return sequence;
		}

		WordPositionsStringBuilder positions = new WordPositionsStringBuilder();

		try (Cursor cursor = db.rawQuery(getPositionsQuery(language, sequence, generations), new String[]{})) {
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


	private String getPositionsQuery(@NonNull Language language, @NonNull String sequence, int generations) {
		return
			"SELECT `start`, `end` FROM ( " +
				getFactoryWordPositionsQuery(language, sequence, generations) +
				") UNION " +
				getCustomWordPositionsQuery(language, sequence, generations);
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
			sql.append(" LIMIT 100"); // @todo: use the longest range per language from TABLE_LANGUAGES_META, but probably converting this to a a compiled query will make it easier
		}

		String positionsSql = sql.toString();
		Logger.d(LOG_TAG, "Index SQL: " + positionsSql);
		return positionsSql;
	}


	@NonNull private String getCustomWordPositionsQuery(@NonNull Language language, @NonNull String sequence, int generations) {
		// @todo: use a compiled query
		String sql = "SELECT -id as `start`, -id as `end` FROM " + TableOperations.CUSTOM_WORDS_TABLE +
			" WHERE langId = " + language.getId() +
			" AND (sequence = " + sequence;

		if (generations > 0) {
			sql += " OR sequence BETWEEN " + sequence + "1 AND " + sequence + "9)";
		} else {
			sql += ")";
		}

		Logger.d(LOG_TAG, "Custom words SQL: " + sql);
		return sql;
	}


	@NonNull private String getWordsQuery(@NonNull Language language, @NonNull String positions, @NonNull String filter, int maximumWords, boolean fullOutput) {
		// @todo: use a compiled query

		StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT word");
		if (fullOutput) {
			sql.append(",frequency,position");
		}

		sql.append(" FROM ").append(TableOperations.getWordsTable(language.getId()))
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


	public int getWordFrequency(@NonNull SQLiteDatabase db, @NonNull Language language, int position) {
		String key = "getWordFrequency_" + language.getId();
		if (!statements.containsKey(key)) {
			statements.put(key, db.compileStatement("SELECT frequency FROM " + TableOperations.getWordsTable(language.getId()) + " WHERE position = ?"));
		}

		SQLiteStatement query = statements.get(key);
		if (query == null) {
			return 0;
		}
		query.bindLong(1, position);

		try {
			return (int)query.simpleQueryForLong();
		} catch (SQLiteDoneException e) {
			return 0;
		}
	}


	public int getNextInNormalizationQueue(@NonNull SQLiteDatabase db) {
		String key = "getNextInNormalizationQueue";
		if (!statements.containsKey(key)) {
			statements.put(key, db.compileStatement("SELECT langId FROM " + TableOperations.LANGUAGES_META_TABLE + " WHERE normalizationPending = 1 LIMIT 1"));
		}

		SQLiteStatement query = statements.get(key);

		try {
			return query == null ? -1 : (int)query.simpleQueryForLong();
		} catch (SQLiteDoneException e) {
			return -1;
		}
	}
}
