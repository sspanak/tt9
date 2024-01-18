package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class WordStore {
	private static final String DB_NAME = "tt9.db";
	private static final int DB_VERSION = 1;
	private static final String WORDS_TABLE_BASE_NAME = "words_";
	private static final String INDEX_TABLE_BASE_NAME = "sequences_";
	private final SQLiteDatabase db;

	public WordStore(Context context) {
		File dbFile = context.getDatabasePath(DB_NAME);
		if (!dbFile.exists()) {
			try {
				dbFile.createNewFile();
			} catch (IOException e) {
				Logger.e("WordStore", "Could not create database file: " + dbFile.getAbsolutePath());
			}
		}

		// @todo: fix the error on Android 6:
		// android.database.sqlite.SQLiteCantOpenDatabaseException: unknown error (code 14): Could not open database
		db = SQLiteDatabase.openDatabase(
			dbFile.getAbsolutePath(),
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

	public ArrayList<String> getMany(Language language, String sequence, String filter, int maximumWords) {
		boolean isFilterOn = filter != null && !filter.isEmpty();

		long startTime = System.currentTimeMillis();

		String indexSql = "SELECT `start`, `end` FROM " + getIndexTableName(language.getId()) + " WHERE ";
		if (sequence.length() == 1) { // @todo: no sequences query here, just search for words
			indexSql += " sequence = " + sequence;
		} else if (sequence.length() == 2 && !isFilterOn) {
			String[] children = new String[10];
			children[0] = sequence;
			for (int i = 1; i <= 9; i++) {
				children[i] = sequence + i;
			}
			indexSql += " sequence IN(" + String.join(",", children) + ")";
		} else if ((sequence.length() == 2 && isFilterOn) || sequence.length() <= 4 && !isFilterOn) {
			String[] children = new String[91];
			children[0] = sequence;
			for (int i = 1; i <= 9; i++) {
				children[i] = sequence + i;
			}

			for (int seqEnd = 11, j = 10; seqEnd <= 99; seqEnd++) {
				if (seqEnd % 10 != 0) {
					children[j++] = sequence + seqEnd;
				}
			}
			indexSql += " sequence IN(" + String.join(",", children) + ")";
		} else {
			indexSql += " sequence = " + sequence + " OR sequence BETWEEN " + sequence + "1 AND " + sequence + "9";
			indexSql += " ORDER BY start ";
			indexSql += " LIMIT 100"; // @todo: maximum number of children + 1 among all sequences
		}

		// @todo: if results for 2-4 < SettingsStore.getSuggestionsMin(), run the >= 4 query

		Logger.d("WordStore", "Index SQL: " + indexSql);

		Cursor cursor = db.rawQuery(indexSql, new String[]{});

		StringBuilder positions = new StringBuilder();

		while (cursor.moveToNext()) {
			int start = cursor.getInt(0);
			int end = cursor.getInt(1);

			if (start < end) {
				for (int position = start; position <= end; position++) {
					positions.append(position);
					if (position < end) {
						positions.append(",");
					}
				}
			} else {
				positions.append(start);
			}

			positions.append(",");
		}

		cursor.close();

		Logger.d("WordStore", "Index time: " + (System.currentTimeMillis() - startTime) + " ms.");

		if (positions.length() == 0) {
			Logger.d("WordStore", "No sequences found. Not searching words.");
			return new ArrayList<>();
		}

		positions.setLength(positions.length() - 1);

		startTime = System.currentTimeMillis();

		String wordsSql =
			"SELECT word " +
			" FROM " + getWordsTableName(language.getId()) +
			" WHERE position IN(" + positions + ")" + (isFilterOn ? " AND word LIKE '" + filter + "%' " : "") +
			" ORDER BY LENGTH(word), frequency DESC " +
			" LIMIT " + maximumWords;
		Logger.d("WordStore", "Words SQL: " + wordsSql);


		ArrayList<String> words = new ArrayList<>();
		cursor = db.rawQuery(wordsSql, new String[]{});
		while (cursor.moveToNext()) {
			words.add(cursor.getString(0));
		}

		cursor.close();

		Logger.d("WordStore", "Words time: " + (System.currentTimeMillis() - startTime) + " ms.");

		return words;
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
		db.execSQL("CREATE INDEX IF NOT EXISTS idx_position_" + langId + " ON " + getWordsTableName(langId) + " (position, word)");
	}

	private void createIndexTable(@NonNull String langId) {
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS " + getIndexTableName(langId) + " (" +
				"sequence TEXT NOT NULL, " +
				"start INTEGER NOT NULL, " +
				"end INTEGER NOT NULL" +
			")"
		);
//		db.execSQL("CREATE INDEX IF NOT EXISTS idx_sequence_" + langId + " ON " + getIndexTableName(langId) + " (sequence)");
		db.execSQL("CREATE INDEX IF NOT EXISTS idx_sequence_start_" + langId + " ON " + getIndexTableName(langId) + " (sequence, `start`)");
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
