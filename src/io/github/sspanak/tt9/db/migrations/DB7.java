package io.github.sspanak.tt9.db.migrations;

import android.content.Context;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.definitions.Bulgarian;
import io.github.sspanak.tt9.languages.definitions.Dutch;
import io.github.sspanak.tt9.languages.definitions.English;
import io.github.sspanak.tt9.languages.definitions.French;
import io.github.sspanak.tt9.languages.definitions.German;
import io.github.sspanak.tt9.languages.definitions.Italian;
import io.github.sspanak.tt9.languages.definitions.Russian;
import io.github.sspanak.tt9.languages.definitions.Spanish;
import io.github.sspanak.tt9.languages.definitions.Ukrainian;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class DB7 {
	private Context ctx;

	public Migration getMigration(Context context) {
		ctx = context;
		return migration;
	}

	private int getNewLanguageId(int oldId) {
		switch (oldId) {
			default:
				return oldId;
			case 1:
				return new English().getId();
			case 2:
				return new Russian().getId();
			case 3:
				return new German().getId();
			case 4:
				return new French().getId();
			case 5:
				return new Italian().getId();
			case 6:
				return new Ukrainian().getId();
			case 7:
				return new Bulgarian().getId();
			case 8:
				return new Dutch().getId();
			case 9:
				return new Spanish().getId();
		}
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
		public void migrate(SupportSQLiteDatabase database) {
			migrateSQL(database);
			migrateSettings();
		}
	};
}
