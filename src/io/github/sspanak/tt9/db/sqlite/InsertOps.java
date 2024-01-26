package io.github.sspanak.tt9.db.sqlite;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.db.entities.Word;
import io.github.sspanak.tt9.db.entities.WordBatch;
import io.github.sspanak.tt9.db.entities.WordPosition;
import io.github.sspanak.tt9.languages.Language;

public class InsertOps {
	private final CompiledQueryCache queryCache;
	private final Language language;

	public InsertOps(SQLiteDatabase db, @NonNull Language language) {
		this.language = language;
		queryCache = CompiledQueryCache.getInstance(db);
	}


	public void insertBatch(@NonNull WordBatch batch) {
		insertBatch(null, batch, -1, -1, 1);
	}


	public void insertBatch(ConsumerCompat<Float> progressCallback, @NonNull WordBatch batch, float minProgress, float maxProgress, int updateInterval) {
		float middleProgress = minProgress + (maxProgress - minProgress) / 2;

		insertWordsBatch(progressCallback, batch.getWords(), minProgress, middleProgress - 2, updateInterval);
		insertWordPositionsBatch(progressCallback, batch.getPositions(), middleProgress - 2, maxProgress - 2, updateInterval);
		insertMaxPositionRange(batch.getMaxPositionRange());

		if (progressCallback != null) progressCallback.accept(maxProgress);
	}


	private void insertMaxPositionRange(int maxPositionRange) {
		SQLiteStatement query = queryCache.get("REPLACE INTO " + Tables.LANGUAGES_META + " (langId, maxPositionRange) VALUES (?, ?)");

		query.bindLong(1, language.getId());
		query.bindLong(2, maxPositionRange);
		query.execute();
	}


	private void insertWordsBatch(ConsumerCompat<Float> progressCallback, ArrayList<Word> wordBatch, float minProgress, float maxProgress, int updateInterval) {
		if (wordBatch.size() == 0) {
			return;
		}

		float progressRatio = (maxProgress - minProgress) / wordBatch.size();

		String sql = "INSERT INTO " + Tables.getWords(language.getId()) + " (frequency, position, word) VALUES (?, ?, ?)";
		SQLiteStatement query = queryCache.get(sql);

		for (int progress = 0, end = wordBatch.size(); progress < end; progress++) {
			Word word = wordBatch.get(progress);
			query.bindLong(1, word.frequency);
			query.bindLong(2, word.position);
			query.bindString(3, word.word);
			query.execute();

			if (progressCallback != null && progress % updateInterval == 0) {
				progressCallback.accept(minProgress + progress * progressRatio);
			}
		}
	}


	private void insertWordPositionsBatch(ConsumerCompat<Float> progressCallback, ArrayList<WordPosition> wordPositionBatch, float minProgress, float maxProgress, int updateInterval) {
		if (wordPositionBatch.size() == 0) {
			return;
		}

		float progressRatio = (maxProgress - minProgress) / wordPositionBatch.size();

		String sql = "INSERT INTO " + Tables.getWordPositions(language.getId()) + " (sequence, `start`, `end`) VALUES (?, ?, ?)";
		SQLiteStatement query = queryCache.get(sql);

		for (int progress = 0, end = wordPositionBatch.size(); progress < end; progress++) {
			WordPosition wordPosition = wordPositionBatch.get(progress);
			query.bindString(1, wordPosition.sequence);
			query.bindLong(2, wordPosition.start);
			query.bindLong(3, wordPosition.end);
			query.execute();

			if (progressCallback != null && progress % updateInterval == 0) {
				progressCallback.accept(minProgress + progress * progressRatio);
			}
		}
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
}
