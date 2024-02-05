package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;


public class UpdateOps {
	private static final String LOG_TAG = UpdateOps.class.getSimpleName();


	public static boolean changeFrequency(@NonNull SQLiteDatabase db, @NonNull Language language, String word, int position, int frequency) {
		String sql = "UPDATE " + Tables.getWords(language.getId()) + " SET frequency = ? WHERE position = ?";

		if (word != null && !word.isEmpty()) {
			sql += " AND word = ?";
		}

		SQLiteStatement query = CompiledQueryCache.get(db, sql);
		query.bindLong(1, frequency);
		query.bindLong(2, position);
		if (word != null && !word.isEmpty()) {
			query.bindString(3, word);
		}

		Logger.v(LOG_TAG, "Change frequency SQL: " + query + "; (" + frequency + ", " + position + ", " + word + ")");

		return query.executeUpdateDelete() > 0;
	}


	public static void normalize(@NonNull SQLiteDatabase db, int langId) {
		if (langId <= 0) {
			return;
		}

		SQLiteStatement query = CompiledQueryCache.get(db, "UPDATE " + Tables.getWords(langId) + " SET frequency = frequency / ?");
		query.bindLong(1, SettingsStore.WORD_FREQUENCY_NORMALIZATION_DIVIDER);
		query.execute();

		query = CompiledQueryCache.get(db, "UPDATE " + Tables.LANGUAGES_META + " SET normalizationPending = ? WHERE langId = ?");
		query.bindLong(1, 0);
		query.bindLong(2, langId);
		query.execute();
	}


	public static void scheduleNormalization(@NonNull SQLiteDatabase db, @NonNull Language language) {
		SQLiteStatement query = CompiledQueryCache.get(db, "UPDATE " + Tables.LANGUAGES_META + " SET normalizationPending = ? WHERE langId = ?");
		query.bindLong(1, 1);
		query.bindLong(2, language.getId());
		query.execute();
	}
}
