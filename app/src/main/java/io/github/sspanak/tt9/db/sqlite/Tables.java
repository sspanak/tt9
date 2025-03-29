package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;

public class Tables {

	static final String LANGUAGES_META = "languages_meta";
	static final String CUSTOM_WORDS = "custom_words";
	private static final String POSITIONS_TABLE_BASE_NAME = "word_positions_";
	private static final String WORDS_TABLE_BASE_NAME = "words_";
	private static final String WORD_PAIRS_TABLE_BASE_NAME = "word_pairs_";

	static String getWords(int langId) { return WORDS_TABLE_BASE_NAME + langId; }
	static String getWordPositions(int langId) { return POSITIONS_TABLE_BASE_NAME + langId; }
	static String getWordPairs(int langId) { return WORD_PAIRS_TABLE_BASE_NAME + langId; }


	static String[] getCreateQueries(ArrayList<Language> languages) {
		int languageCount = languages.size();
		String[] queries = new String[languageCount * 3 + 3];

		queries[0] = createCustomWords();
		queries[1] = createCustomWordsIndex();
		queries[2] = createLanguagesMeta();

		int queryId = 3;
		for (Language language : languages) {
			queries[queryId++] = createWordsTable(language.getId());
			queries[queryId++] = createWordPositions(language.getId());
			queries[queryId++] = createWordPairs(language.getId());
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
			"CREATE TABLE IF NOT EXISTS " + getWords(langId) + " (" +
				"frequency INTEGER NOT NULL DEFAULT 0, " +
				"position INTEGER NOT NULL, " +
				"word TEXT NOT NULL" +
			")";
	}

	private static String createWordsIndex(int langId) {
		return "CREATE INDEX IF NOT EXISTS idx_position_" + langId + " ON " + getWords(langId) + " (position, word)";
	}

	private static String dropWordsIndex(int langId) {
		return "DROP INDEX IF EXISTS idx_position_" + langId;
	}

	private static String createWordPositions(int langId) {
		return
			"CREATE TABLE IF NOT EXISTS " + getWordPositions(langId) + " (" +
				"sequence TEXT NOT NULL, " +
				"start INTEGER NOT NULL, " +
				"end INTEGER NOT NULL" +
			")";
	}

	private static String createWordsPositionsIndex(int langId) {
		return "CREATE INDEX IF NOT EXISTS idx_sequence_start_" + langId + " ON " + getWordPositions(langId) + " (sequence, `start`)";
	}

	private static String dropWordPositionsIndex(int langId) {
		return "DROP INDEX IF EXISTS idx_sequence_start_" + langId;
	}

	private static String createCustomWords() {
		return "CREATE TABLE IF NOT EXISTS " + CUSTOM_WORDS + " (" +
			"id INTEGER PRIMARY KEY, " +
			"langId INTEGER NOT NULL, " +
			"sequence TEXT NOT NULL, " +
			"word INTEGER NOT NULL " +
		")";
	}

	private static String createCustomWordsIndex() {
		return "CREATE INDEX IF NOT EXISTS idx_langId_sequence ON " + CUSTOM_WORDS + " (langId, sequence)";
	}

	private static String createWordPairs(int langId) {
		return "CREATE TABLE IF NOT EXISTS " + getWordPairs(langId) + " (" +
			"word1 TEXT NOT NULL, " +
			"word2 TEXT NOT NULL, " +
			"sequence2 TEXT NOT NULL " +
		")";
	}

	private static String createLanguagesMeta() {
		return "CREATE TABLE IF NOT EXISTS " + LANGUAGES_META + " (" +
			"langId INTEGER UNIQUE NOT NULL, " +
			"positionsToNormalize TEXT NULL," +
			"fileHash TEXT NOT NULL DEFAULT 0 " +
		")";
	}
}
