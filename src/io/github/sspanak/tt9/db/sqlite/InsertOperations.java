package io.github.sspanak.tt9.db.sqlite;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.entities.Word;
import io.github.sspanak.tt9.db.entities.WordPosition;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.Language;

public class InsertOperations {
	private final CompiledQueryCache queryCache;
	private final Language language;
	private WordPosition lastWordPosition;
	private int maxPositionRange;
	@NonNull public final ArrayList<Word> wordsBatch = new ArrayList<>();
	@NonNull public final ArrayList<WordPosition> wordPositionsBatch = new ArrayList<>();


	public InsertOperations(SQLiteDatabase db, @NonNull Language language) {
		this.language = language;
		queryCache = new CompiledQueryCache(db);
	}


	/**
	 * Adds a word and a digit sequence to the end of the internal ArrayLists, assuming this is called
	 * repeatedly with properly sorted data.
	 * It is a bit counterintuitive, but accumulating the data in ArrayLists, then using
	 * saveWordsBatch() and saveWordPositionsBatch() is about 30% faster than using query.exectute()
	 * for saving each word and sequence one by one.
	 */
	public void addWordToBatch(@NonNull String word, int frequency, int position, int maxSize) throws InvalidLanguageCharactersException {
		wordsBatch.add(Word.create(word, frequency, position));

		if (position == 0) {
			return;
		}

		String sequence = language.getDigitSequenceForWord(word);

		if (position == 1 || lastWordPosition == null) {
			lastWordPosition = WordPosition.create(sequence, position);
		} else {
			lastWordPosition.end = position;
		}

		if (!sequence.equals(lastWordPosition.sequence)) {
			lastWordPosition.end--;
			wordPositionsBatch.add(lastWordPosition);

			if (wordPositionsBatch.size() >= maxSize) {
				maxPositionRange = Math.max(maxPositionRange, lastWordPosition.getRangeLength());
				saveBatch();
			}

			lastWordPosition = WordPosition.create(sequence, position);
		}
	}


	public void finalizeBatchSave() {
		if (lastWordPosition == null) {
			return;
		}

		saveBatch();
		saveMaxPositionRange();
	}


	public void clearBatch() {
		lastWordPosition = null;
		maxPositionRange = 0;
		wordsBatch.clear();
		wordPositionsBatch.clear();
	}


	private void saveBatch() {
		saveWordsBatch();
		saveWordPositionsBatch();
		clearBatch();
	}


	private void saveMaxPositionRange() {
		String sql = "REPLACE INTO " + TableOperations.LANGUAGES_META_TABLE + " (langId, maxPositionRange) VALUES (?, ?)";
		SQLiteStatement query = queryCache.getOrCreate(sql);

		query.bindLong(1, language.getId());
		query.bindLong(2, maxPositionRange);
		query.execute();
	}


	private void saveWordsBatch() {
		if (wordsBatch.size() == 0) {
			return;
		}

		String sql = "INSERT INTO " + TableOperations.getWordsTable(language.getId()) + " (frequency, position, word) VALUES (?, ?, ?)";
		SQLiteStatement query = queryCache.getOrCreate(sql);
		for (Word word : wordsBatch) {
			query.bindLong(1, word.frequency);
			query.bindLong(2, word.position);
			query.bindString(3, word.word);
			query.execute();
		}
	}


	private void saveWordPositionsBatch() {
		if (wordPositionsBatch.size() == 0) {
			return;
		}

		String sql = "INSERT INTO " + TableOperations.getWordPositionsTable(language.getId()) + " (sequence, `start`, `end`) VALUES (?, ?, ?)";
		SQLiteStatement query = queryCache.getOrCreate(sql);
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
