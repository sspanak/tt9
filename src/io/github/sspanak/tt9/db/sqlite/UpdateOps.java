package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;


public class UpdateOps {
	private static final String LOG_TAG = UpdateOps.class.getSimpleName();

	public static boolean changeFrequency(@NonNull SQLiteDatabase db, @NonNull Language language, int position, int frequency) {
		CompiledQueryCache cache = CompiledQueryCache.getInstance(db);

		SQLiteStatement query = cache.get("UPDATE " + Tables.getWords(language.getId()) + " SET frequency = ? WHERE position = ?");
		query.bindLong(1, frequency);
		query.bindLong(2, position);
		query.execute();

		Logger.v(LOG_TAG, "Change frequency SQL: " + query + "; (" + frequency + ", " + position + ")");

		return cache.simpleQueryForLong("SELECT changes()", 0) > 0;
	}


	public static void normalize(@NonNull SQLiteDatabase db, @NonNull SettingsStore settings, int langId) {
		if (langId <= 0) {
			return;
		}

		CompiledQueryCache cache = CompiledQueryCache.getInstance(db);

		SQLiteStatement query = cache.get("UPDATE " + Tables.getWords(langId) + " SET frequency = frequency / ?");
		query.bindLong(1, settings.getWordFrequencyNormalizationDivider());
		query.executeUpdateDelete();

		query = cache.get("UPDATE " + Tables.LANGUAGES_META + " SET normalizationPending = ? WHERE langId = ?");
		query.bindLong(1, 0);
		query.bindLong(2, langId);
		query.executeUpdateDelete();
	}


	public static void scheduleNormalization(@NonNull SQLiteDatabase db, @NonNull Language language) {
		CompiledQueryCache cache = CompiledQueryCache.getInstance(db);
		SQLiteStatement query = cache.get("UPDATE " + Tables.LANGUAGES_META + " SET normalizationPending = ? WHERE langId = ?");
		query.bindLong(1, 1);
		query.bindLong(2, language.getId());
		query.executeUpdateDelete();
	}
}
