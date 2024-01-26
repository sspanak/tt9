package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;

public class TableOperations {

	static final String LANGUAGES_META_TABLE = "languages_meta";
	static final String CUSTOM_WORDS_TABLE = "custom_words";
	private static final String POSITIONS_TABLE_BASE_NAME = "word_positions_";
	private static final String WORDS_TABLE_BASE_NAME = "words_";

	static String getWordsTable(int langId) { return WORDS_TABLE_BASE_NAME + langId; }
	static String getWordPositionsTable(int langId) { return POSITIONS_TABLE_BASE_NAME + langId; }


	static String[] getCreateQueries(ArrayList<Language> languages) {
		int languageCount = languages.size();
		String[] queries = new String[languageCount * 4 + 3];

		queries[0] = createCustomWords();
		queries[1] = createCustomWordsIndex();
		queries[2] = createLanguagesMeta();

		int queryId = 3;
		for (Language language : languages) {
			queries[queryId++] = createWordsTable(language.getId());
			queries[queryId++] = createWordsIndex(language.getId());
			queries[queryId++] = createWordPositions(language.getId());
			queries[queryId++] = createWordsPositionsIndex(language.getId());
		}

		return queries;
	}


	public static void createWordIndex(@NonNull SQLiteDatabase db, @NonNull Language language) {
		CompiledQueryCache.execute(db, createWordsIndex(language.getId()));
	}

	public static void createPositionIndex(@NonNull SQLiteDatabase db, @NonNull Language language) {
		CompiledQueryCache.execute(db, createWordsPositionsIndex(language.getId()));
	}


	public static void dropIndexes(@NonNull SQLiteDatabase db, @NonNull Language language) {
		CompiledQueryCache
			.execute(db, dropWordsIndex(language.getId()))
			.execute(dropWordPositionsIndex(language.getId()));
	}


	private static String createWordsTable(int langId) {
		return
			"CREATE TABLE IF NOT EXISTS " + getWordsTable(langId) + " (" +
				"frequency INTEGER NOT NULL DEFAULT 0, " +
				"position INTEGER NOT NULL, " +
				"word TEXT NOT NULL" +
			")";
	}

	private static String createWordsIndex(int langId) {
		return "CREATE INDEX IF NOT EXISTS idx_position_" + langId + " ON " + getWordsTable(langId) + " (position, word)";
	}

	private static String dropWordsIndex(int langId) {
		return "DROP INDEX IF EXISTS idx_position_" + langId;
	}

	private static String createWordPositions(int langId) {
		return
			"CREATE TABLE IF NOT EXISTS " + getWordPositionsTable(langId) + " (" +
				"sequence TEXT NOT NULL, " +
				"start INTEGER NOT NULL, " +
				"end INTEGER NOT NULL" +
			")";
	}

	private static String createWordsPositionsIndex(int langId) {
		return "CREATE INDEX IF NOT EXISTS idx_sequence_start_" + langId + " ON " + getWordPositionsTable(langId) + " (sequence, `start`)";
	}

	private static String dropWordPositionsIndex(int langId) {
		return "DROP INDEX IF EXISTS idx_sequence_start_" + langId;
	}

	private static String createCustomWords() {
		return "CREATE TABLE IF NOT EXISTS " + CUSTOM_WORDS_TABLE + " (" +
			"id INTEGER PRIMARY KEY, " +
			"langId INTEGER NOT NULL, " +
			"sequence TEXT NOT NULL, " +
			"word INTEGER NOT NULL " +
		")";
	}

	private static String createCustomWordsIndex() {
		return "CREATE INDEX IF NOT EXISTS idx_langId_sequence ON " + CUSTOM_WORDS_TABLE + " (langId, sequence)";
	}

	private static String createLanguagesMeta() {
		return "CREATE TABLE IF NOT EXISTS " + LANGUAGES_META_TABLE + " (" +
			"langId INTEGER UNIQUE NOT NULL, " +
			"maxPositionRange INTEGER NOT NULL DEFAULT 0, " +
			"normalizationPending INT2 NOT NULL DEFAULT 0 " +
		")";
	}
}
