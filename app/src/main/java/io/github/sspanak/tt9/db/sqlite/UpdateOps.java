package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.db.entities.NormalizationList;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;


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


	public static void normalize(@NonNull SQLiteDatabase db, NormalizationList normalizationList) {
		if (normalizationList.langId <= 0 || normalizationList.positions == null || normalizationList.positions.isEmpty()) {
			return;
		}

		db.execSQL(
			"UPDATE " + Tables.getWords(normalizationList.langId) +
			" SET frequency = frequency / " + SettingsStore.WORD_FREQUENCY_NORMALIZATION_DIVIDER +
			" WHERE position IN (" + normalizationList.positions + ")"
		);

		SQLiteStatement query = CompiledQueryCache.get(db, "UPDATE " + Tables.LANGUAGES_META + " SET positionsToNormalize = NULL WHERE langId = ?");
		query.bindLong(1, normalizationList.langId);
		query.execute();
	}


	public static void scheduleNormalization(@NonNull SQLiteDatabase db, @NonNull Language language, @NonNull String positions) {
		SQLiteStatement query = CompiledQueryCache.get(db, "UPDATE " + Tables.LANGUAGES_META + " SET positionsToNormalize = ? WHERE langId = ?");
		query.bindString(1, positions);
		query.bindLong(2, language.getId());
		query.execute();
	}
}
