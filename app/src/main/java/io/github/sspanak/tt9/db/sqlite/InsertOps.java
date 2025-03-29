package io.github.sspanak.tt9.db.sqlite;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import java.util.Collection;

import io.github.sspanak.tt9.db.entities.Word;
import io.github.sspanak.tt9.db.entities.WordPosition;
import io.github.sspanak.tt9.db.wordPairs.WordPair;
import io.github.sspanak.tt9.languages.Language;


public class InsertOps {
	private final SQLiteStatement insertWordsQuery;
	private final SQLiteStatement insertPositionsQuery;


	public InsertOps(SQLiteDatabase db, @NonNull Language language) {
		// super cache to avoid String concatenation in the dictionary loading loop
		insertWordsQuery = CompiledQueryCache.get(db, "INSERT INTO " + Tables.getWords(language.getId()) + " (frequency, position, word) VALUES (?, ?, ?)");
		insertPositionsQuery = CompiledQueryCache.get(db, "INSERT INTO " + Tables.getWordPositions(language.getId()) + " (sequence, `start`, `end`) VALUES (?, ?, ?)");
	}


	public void insertWord(Word word) {
		insertWordsQuery.bindLong(1, word.frequency);
		insertWordsQuery.bindLong(2, word.position);
		insertWordsQuery.bindString(3, word.word);
		insertWordsQuery.execute();
	}


	public void insertWordPosition(WordPosition position) {
		insertPositionsQuery.bindString(1, position.sequence);
		insertPositionsQuery.bindLong(2, position.start);
		insertPositionsQuery.bindLong(3, position.end);
		insertPositionsQuery.execute();
	}


	public static void replaceLanguageMeta(@NonNull SQLiteDatabase db, int langId, String fileHash) {
		SQLiteStatement query = CompiledQueryCache.get(db, "REPLACE INTO " + Tables.LANGUAGES_META + " (langId, fileHash) VALUES (?, ?)");
		query.bindLong(1, langId);
		query.bindString(2, fileHash);
		query.execute();
	}


	public static boolean insertCustomWord(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String sequence, @NonNull String word) {
		ContentValues values = new ContentValues();
		values.put("langId", language.getId());
		values.put("sequence", sequence);
		values.put("word", word);

		long insertId = db.insert(Tables.CUSTOM_WORDS, null, values);
		if (insertId == -1) {
			return false;
		}

		// If the user inserts more than 2^31 custom words, the "position" will overflow and will mess up
		// the words table, but realistically it will never happen, so we don't bother preventing it.

		values = new ContentValues();
		values.put("position", (int)-insertId);
		values.put("word", word);
		insertId = db.insert(Tables.getWords(language.getId()), null, values);

		return insertId != -1;
	}


	public static void restoreCustomWords(@NonNull SQLiteDatabase db, @NonNull Language language) {
		CompiledQueryCache.execute(
			db,
			"INSERT INTO " + Tables.getWords(language.getId()) + " (position, word) " +
				"SELECT -id, word FROM " + Tables.CUSTOM_WORDS + " WHERE langId = " + language.getId()
		);
	}

	public static void insertWordPairs(@NonNull SQLiteDatabase db, int langId, Collection<WordPair> pairs) {
		if (langId <= 0 || pairs == null || pairs.isEmpty()) {
			return;
		}

		StringBuilder sql = new StringBuilder(
			"INSERT INTO " + Tables.getWordPairs(langId) + " (word1, word2, sequence2) VALUES"
		);

		for (WordPair pair : pairs) {
			sql.append(pair.toSqlRow()).append(",");
		}

		sql.setLength(sql.length() - 1);

		db.execSQL(sql.toString());
	}
}
