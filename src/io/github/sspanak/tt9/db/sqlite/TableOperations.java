package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class TableOperations {

	static final String CUSTOM_WORDS_TABLE = "custom_words";
	private static final String POSITIONS_TABLE_BASE_NAME = "word_positions_";
	private static final String WORDS_TABLE_BASE_NAME = "words_";

	static String getWordsTable(int langId) { return WORDS_TABLE_BASE_NAME + langId; }
	static String getWordPositionsTable(int langId) { return POSITIONS_TABLE_BASE_NAME + langId; }


	static String[] getCreateQueries(Context context) {
		int languageCount = LanguageCollection.count(context);
		String[] queries = new String[languageCount * 4 + 2];

		queries[0] = createCustomWords();
		queries[1] = createCustomWordsIndex();

		int queryId = 2;
		for (Language language : LanguageCollection.getAll(context)) {
			queries[queryId++] = createWordsTable(language.getId());
			queries[queryId++] = createWordsIndex(language.getId());
			queries[queryId++] = createWordPositions(language.getId());
			queries[queryId++] = createWordsPositionsIndex(language.getId());
		}

		return queries;
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
}
