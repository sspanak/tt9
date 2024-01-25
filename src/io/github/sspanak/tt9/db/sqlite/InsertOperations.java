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
	private final String INSERT_INTO_WORDS_SQL;
	private final String INSERT_INTO_WORD_POSITIONS_SQL;
	private final CompiledQueryCache queryCache;
	private final int MAX_SIZE;
	private boolean isFull;
	private final Language language;
	private WordPosition lastWordPosition;
	private int maxPositionRange;
	@NonNull public final ArrayList<Word> wordsBatch = new ArrayList<>();
	@NonNull public final ArrayList<WordPosition> wordPositionsBatch = new ArrayList<>();

	private SQLiteDatabase db;


	public InsertOperations(SQLiteDatabase db, @NonNull Language language, @NonNull SettingsStore settings) {
		this.db = db;
		this.language = language;
		MAX_SIZE = settings.getDictionaryImportWordChunkSize();
		queryCache = new CompiledQueryCache(db);

		INSERT_INTO_WORDS_SQL = "INSERT INTO " + TableOperations.getWordsTable(language.getId()) + " (frequency, position, word) VALUES (?, ?, ?)";
		INSERT_INTO_WORD_POSITIONS_SQL = "INSERT INTO " + TableOperations.getWordPositionsTable(language.getId()) + " (sequence, `start`, `end`) VALUES (?, ?, ?)";
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
			lastWordPosition.end = (position - 1);
			maxPositionRange = Math.max(maxPositionRange, lastWordPosition.getRangeLength());

			isFull = wordPositionsBatch.size() >= MAX_SIZE;
			if (!isFull) {
				wordPositionsBatch.add(lastWordPosition);
				lastWordPosition = WordPosition.create(sequence, position);
			}
		}

		return !isFull;
	}


	public void clearBatch() {
		isFull = false;
		lastWordPosition = null;
		maxPositionRange = 0;
		wordsBatch.clear();
		wordPositionsBatch.clear();
	}


	public void saveMaxPositionRange() {
		String sql = "REPLACE INTO " + TableOperations.LANGUAGES_META_TABLE + " (langId, maxPositionRange) VALUES (?, ?)";
		SQLiteStatement query = queryCache.getOrCreate(sql);

		query.bindLong(1, language.getId());
		query.bindLong(2, maxPositionRange);
		query.execute();
	}


	public void saveBatch() {
		saveWordsBatch();
		saveWordPositionsBatch();
		clearBatch();
	}


	private void saveWordsBatch() {
		if (wordsBatch.size() == 0) {
			return;
		}

		SQLiteStatement query = queryCache.getOrCreate(this.INSERT_INTO_WORDS_SQL);
		for (Word word : wordsBatch) {
			query.bindLong(1, word.frequency);
			query.bindLong(2, word.position);
			query.bindString(3, word.word);
			query.execute();
		}
	}

	private void addWord(@NonNull Word word) {

	}


	private void saveWordPositionsBatch() {
		if (wordPositionsBatch.size() == 0) {
			return;
		}

		SQLiteStatement query = queryCache.getOrCreate(INSERT_INTO_WORD_POSITIONS_SQL);
		for (WordPosition wordPosition : wordPositionsBatch) {
			query.bindString(1, wordPosition.sequence);
			query.bindLong(2, wordPosition.start);
			query.bindLong(3, wordPosition.end);
			query.execute();
		}
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

		// If the user inserts more than 2^31 custom words, the "position" will overflow and will mess up
		// the words table, but realistically it will never happen, so we don't bother preventing it.

		values = new ContentValues();
		values.put("position", (int)-insertId);
		values.put("word", word);
		insertId = db.insert(TableOperations.getWordsTable(language.getId()), null, values);

		return insertId != -1;
	}


	public void restoreCustomWords() {
		String sql =
				"INSERT INTO " + TableOperations.getWordsTable(language.getId()) + " (position, word) " +
				"SELECT -id, word FROM " + TableOperations.CUSTOM_WORDS_TABLE + " WHERE langId = " + language.getId();

		queryCache.getOrCreate(sql).execute();
	}
}
