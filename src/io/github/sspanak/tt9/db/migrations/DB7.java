package io.github.sspanak.tt9.db.migrations;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.Locale;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class DB7 {
	private Context ctx;

	public Migration getMigration(Context context) {
		ctx = context;
		return migration;
	}

	private int getNewLanguageId(int oldId) {
		Language language;

		switch (oldId) {
			default:
				return oldId;
			case 1:
				language = LanguageCollection.getByLocale(ctx, Locale.ENGLISH.toString());
				break;
			case 2:
				language = LanguageCollection.getByLocale(ctx, "ru_RU");
				break;
			case 3:
				language = LanguageCollection.getByLocale(ctx, Locale.GERMAN.toString());
				break;
			case 4:
				language = LanguageCollection.getByLocale(ctx, Locale.FRENCH.toString());
				break;
			case 5:
				language = LanguageCollection.getByLocale(ctx, Locale.ITALIAN.toString());
				break;
			case 6:
				language = LanguageCollection.getByLocale(ctx, "uk_UA");
				break;
			case 7:
				language = LanguageCollection.getByLocale(ctx, "bg_BG");
				break;
			case 8:
				language = LanguageCollection.getByLocale(ctx, "nl_NL");
				break;
			case 9:
				language = LanguageCollection.getByLocale(ctx, "es_ES");
				break;
		}

		return language != null ? language.getId() : -1;
	}

	private final Migration migration = new Migration(6, 7) {
		private void migrateSQL(SupportSQLiteDatabase database) {
			for (int oldLangId = 1; oldLangId <= 9; oldLangId++) {
				database.execSQL(
					"UPDATE words " +
					" SET lang = " + getNewLanguageId(oldLangId) +
					" WHERE lang = " + oldLangId
				);
			}
		}

		private void migrateSettings() {
			SettingsStore settings = new SettingsStore(ctx);

			ArrayList<Integer> newLangIds = new ArrayList<>();
			for (int langId : settings.getEnabledLanguageIds()) {
				newLangIds.add(getNewLanguageId(langId));
			}

			settings.saveEnabledLanguageIds(newLangIds);
		}

		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			migrateSQL(database);
			migrateSettings();
		}
	};
}
