package io.github.sspanak.tt9.db.sqlite;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.sspanak.tt9.db.entities.Word;
import io.github.sspanak.tt9.db.entities.WordPosition;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class InsertOperations {
	private static final HashMap<String, SQLiteStatement> statements = new HashMap<>();

	private final int MAX_SIZE;
	private boolean isFull;
	private final Language language;
	private WordPosition lastWordPosition;
	@NonNull public final ArrayList<Word> wordsBatch = new ArrayList<>();
	@NonNull public final ArrayList<WordPosition> wordPositionsBatch = new ArrayList<>();


	public InsertOperations(@NonNull Language language, @NonNull SettingsStore settings) {
		this.language = language;
		MAX_SIZE = settings.getDictionaryImportWordChunkSize();
	}


	/**
	 * Adds a word and a digit sequence to the end of the internal lists, assuming this is called
	 * repeatedly with properly sorted data.
	 * <p>
	 * When the batch becomes full, this will refuse to add more words and return false. In this case,
	 * you must call save() to store the words in the database and clear the batch.
	 */
	public boolean addWordToBatch(@NonNull String word, short frequency, int position) throws InvalidLanguageCharactersException {
		if (isFull) {
			return false;
		}

		wordsBatch.add(Word.create(word, frequency, position));
		String sequence = language.getDigitSequenceForWord(word);
		if (position == 0) {
			return true;
		}

		if (position == 1 || lastWordPosition == null) {
			lastWordPosition = WordPosition.create(sequence, position);
		}

		if (!sequence.equals(lastWordPosition.sequence)) {
			lastWordPosition.endAt(position - 1);

			isFull = wordPositionsBatch.size() >= MAX_SIZE;
			if (!isFull) {
				wordPositionsBatch.add(lastWordPosition);
				lastWordPosition = WordPosition.create(sequence, position);
			}
		}

		return !isFull;
	}

	private void clearBatch() {
		isFull = false;
		lastWordPosition = null;
		wordsBatch.clear();
		wordPositionsBatch.clear();
	}

	public void saveBatch(@NonNull SQLiteDatabase db) {
		// @todo: try using multiple threads for speeding up the process
		saveWordsBatch(db);
		saveWordPositionsBatch(db);
		clearBatch();
	}


	private void saveWordsBatch(SQLiteDatabase db) {
		// @todo: use a compiled statement for inserting. more info: https://stackoverflow.com/questions/1711631/improve-insert-per-second-performance-of-sqlite
		if (wordsBatch.size() == 0) {
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO " + TableOperations.getWordsTable(language.getId()) + " (frequency, position, word) VALUES ");
		for (Word word : wordsBatch) {
			sql
				.append("(")
				.append(word.frequency).append(",").append(word.position).append(",'").append(word.word.replaceAll("'", "''")).append("'")
				.append("),");
		}
		sql.setLength(sql.length() - 1);

		db.execSQL(sql.toString());
	}


	private void saveWordPositionsBatch(SQLiteDatabase db) {
		// @todo: use a compiled statement for inserting. see above.
		if (wordPositionsBatch.size() == 0) {
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO " + TableOperations.getWordPositionsTable(language.getId()) + " (sequence, start, end) VALUES ");

		for (WordPosition pos : wordPositionsBatch) {
			sql
				.append("(")
				.append(pos.sequence).append(",").append(pos.start).append(",").append(pos.end)
				.append("),");
		}
		sql.setLength(sql.length() - 1);

		db.execSQL(sql.toString());
	}


	public static boolean insertCustomWord(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String sequence, @NonNull String word) {
		ContentValues values = new ContentValues();
		values.put("langId", language.getId());
		values.put("sequence", sequence);
		values.put("word", word);

		long insertId = db.insert(TableOperations.CUSTOM_WORDS_TABLE, null, values);
		if (insertId == -1) {
			return false;
		}

		values = new ContentValues();
		values.put("position", (int)-insertId);
		values.put("word", word);
		db.insert(TableOperations.getWordsTable(language.getId()), null, values);

		return true;
	}


	public static void restoreCustomWords(@NonNull SQLiteDatabase db, @NonNull Language language) {
		String key = "restoreCustomWords_" + language.getId();
		if (!statements.containsKey(key)) {
			String sql =
				"INSERT INTO " + TableOperations.getWordsTable(language.getId()) + " (position, word) " +
				"SELECT -id, word FROM " + TableOperations.CUSTOM_WORDS_TABLE + " WHERE langId = " + language.getId();
			statements.put(key, db.compileStatement(sql));
		}

		SQLiteStatement query = statements.get(key);
		if (query != null) {
			query.execute();
		}
	}
}
