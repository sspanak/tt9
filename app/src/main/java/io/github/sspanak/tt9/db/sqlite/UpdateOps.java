package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.preferences.SettingsStore;


public class UpdateOps {
	private static final String LOG_TAG = UpdateOps.class.getSimpleName();


	public static boolean changeFrequency(@NonNull SQLiteDatabase db, @NonNull Language language, Text wordFilter, int position, int frequency) {
		String sql = "UPDATE " + Tables.getWords(language.getId()) + " SET frequency = ? WHERE position = ?";

		if (wordFilter != null && !wordFilter.isEmpty()) {
			sql += " AND word IN(?, ?, ?)";
		}

		SQLiteStatement query = CompiledQueryCache.get(db, sql);
		query.bindLong(1, frequency);
		query.bindLong(2, position);
		if (wordFilter != null && !wordFilter.isEmpty()) {
			query.bindString(3, wordFilter.capitalize());
			query.bindString(4, wordFilter.toLowerCase());
			query.bindString(5, wordFilter.toUpperCase());
		}

		Logger.v(LOG_TAG, "Change frequency SQL: " + query + "; (" + frequency + ", " + position + ", " + wordFilter + ")");

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
