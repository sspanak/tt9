package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.objectbox.WordList;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class WordStore {
	private static final String DB_NAME = "tt9.db";
	private static final int DB_VERSION = 1;
	private static final String WORDS_TABLE_BASE_NAME = "words_";
	private static final String INDEX_TABLE_BASE_NAME = "sequences_";
	private final SQLiteDatabase db;

	public WordStore(Context context) {
		db = SQLiteDatabase.openDatabase(
			context.getDatabasePath(DB_NAME).getAbsolutePath(),
			null,
			SQLiteDatabase.CREATE_IF_NECESSARY | SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING
		);
		createActiveLanguageTables(context);
	}

	public void put(@NonNull Language language, @NonNull Word word, @NonNull SequenceRange sequenceRange) {

	}

	public void put(@NonNull Language language, @NonNull WordBatch batch) {
		insertWords(language, batch.words);
		insertSequences(language, batch.sequences);
	}

	public void beginTransaction() {
		db.beginTransactionNonExclusive();
	}

	public void failTransaction() {
		db.endTransaction();
	}

	public void finishTransaction() {
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public WordList getMany(Language language, String sequence, String filter, int maximumWords) {
		return new WordList();
	}

	private String getWordsTableName(@NonNull String langId) {
		return WORDS_TABLE_BASE_NAME + langId;
	}
	private String getWordsTableName(int langId) {
		return WORDS_TABLE_BASE_NAME + langId;
	}
	private String getIndexTableName(@NonNull String langId) {
		return INDEX_TABLE_BASE_NAME + langId;
	}
	private String getIndexTableName(int langId) {
		return INDEX_TABLE_BASE_NAME + langId;
	}

	private void createWordsTable(@NonNull String langId) {
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS " + getWordsTableName(langId) + " (" +
				"frequency INTEGER NOT NULL DEFAULT 0, " +
				"isCustom TINYINT NOT NULL DEFAULT 0, " +
				"position INTEGER NOT NULL, " +
				"word TEXT NOT NULL" +
			")"
		);
		db.execSQL("CREATE INDEX IF NOT EXISTS idx_position ON " + getWordsTableName(langId) + " (position)");
	}

	private void createIndexTable(@NonNull String langId) {
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS " + getIndexTableName(langId) + " (" +
				"sequence TEXT NOT NULL, " +
				"start INTEGER NOT NULL, " +
				"end INTEGER NOT NULL" +
			")"
		);
		db.execSQL("CREATE INDEX IF NOT EXISTS idx_sequence ON " + getIndexTableName(langId) + " (sequence)");
	}

	private void createActiveLanguageTables(Context context) {
		db.beginTransactionNonExclusive();
		for (Language language : LanguageCollection.getAll(context)) {
			String id = String.valueOf(language.getId());
			createWordsTable(id);
			createIndexTable(id);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	private void insertWords(Language language, ArrayList<Word> words) {
		if (words.size() == 0) {
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO " + getWordsTableName(language.getId()) + " (frequency, position, word) VALUES ");
		int count = 0;
		int commaLimit = words.size() - 1;
		for (Word word : words) {
			sql.append("(").append(word.frequency).append(",").append(word.position).append(",'").append(word.word.replaceAll("'", "''")).append("')");
			if (count++ < commaLimit) {
				sql.append(",");
			}
		}

		db.execSQL(sql.toString());
	}

	private void insertSequences(Language language, ArrayList<SequenceRange> sequences) {
		if (sequences.size() == 0) {
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO " + getIndexTableName(language.getId()) + " (sequence, start, end) VALUES ");
		int count = 0;
		int commaLimit = sequences.size() - 1;
		for (SequenceRange seq : sequences) {
			sql.append("(").append(seq.sequence).append(",").append(seq.start).append(",").append(seq.end).append(")");
			if (count++ < commaLimit) {
				sql.append(",");
			}
		}

		db.execSQL(sql.toString());
	}
}
