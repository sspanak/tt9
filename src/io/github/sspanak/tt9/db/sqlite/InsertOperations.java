package io.github.sspanak.tt9.db.sqlite;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.db.entities.WordPosition;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.Language;

public class InsertOperations {
	private final Language language;
	private final CompiledQueryCache queryCache;
	private WordPosition lastWordPosition = null;
	private int maxPositionRange;


	public InsertOperations(@NonNull SQLiteDatabase db, @NonNull Language language) {
		this.language = language;
		queryCache = new CompiledQueryCache(db);
	}


	public static boolean addCustomWord(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String sequence, @NonNull String word) {
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


	public void addWordInBatch(@NonNull String word, int frequency, int position) throws InvalidLanguageCharactersException {
		if (position == 0) {
			return;
		}

//		insertWord(frequency, position, word);

		String sequence = language.getDigitSequenceForWord(word);

		if (position == 1 || lastWordPosition == null) {
			lastWordPosition = WordPosition.create(sequence, position);
		}

		lastWordPosition.end = position;

		if (!sequence.equals(lastWordPosition.sequence)) {
			lastWordPosition.end--;
			endBatchRange();
			lastWordPosition = WordPosition.create(sequence, position);
		}
	}


	public void finalizeBatch() {
//		endBatchRange();
//		saveMaxPositionRange();
		lastWordPosition = null;
		maxPositionRange = 0;
	}

	private void endBatchRange() {
		maxPositionRange = Math.max(maxPositionRange, lastWordPosition.getRangeLength());
//		insertWordPosition(lastWordPosition);
	}


	private void saveMaxPositionRange() {
		String sql = "REPLACE INTO " + TableOperations.LANGUAGES_META_TABLE + " (langId, maxPositionRange) VALUES (?, ?)";
		SQLiteStatement query = queryCache.getOrCreate(sql);

		query.bindLong(1, language.getId());
		query.bindLong(2, maxPositionRange);
		query.execute();
	}


	private void insertWord(int frequency, int position, @NonNull String word) {
		String sql = "INSERT INTO " + TableOperations.getWordsTable(language.getId()) + " (frequency, position, word) VALUES (?, ?, ?)";
		SQLiteStatement query = queryCache.getOrCreate(sql);

		query.bindLong(1, frequency);
		query.bindLong(2, position);
		query.bindString(3, word);
		query.execute();
	}


	private void insertWordPosition(@NonNull WordPosition position) {
		String sql = "INSERT INTO " + TableOperations.getWordPositionsTable(language.getId()) + " (sequence, `start`, `end`) VALUES (?, ?, ?)";
		SQLiteStatement query = queryCache.getOrCreate(sql);

		query.bindString(1, position.sequence);
		query.bindLong(2, position.start);
		query.bindLong(3, position.end);
		query.execute();
	}
}
