package io.github.sspanak.tt9.db.room;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import io.github.sspanak.tt9.languages.definitions.Bulgarian;
import io.github.sspanak.tt9.languages.definitions.Dutch;
import io.github.sspanak.tt9.languages.definitions.English;
import io.github.sspanak.tt9.languages.definitions.French;
import io.github.sspanak.tt9.languages.definitions.German;
import io.github.sspanak.tt9.languages.definitions.Italian;
import io.github.sspanak.tt9.languages.definitions.Russian;
import io.github.sspanak.tt9.languages.definitions.Spanish;
import io.github.sspanak.tt9.languages.definitions.Ukrainian;

public class Migrations {
	public static final Migration v5_v6 = new Migration(5, 6) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
			database.execSQL("DROP TRIGGER IF EXISTS normalize_freq");
		}
	};

	public static final Migration v6_v7 = new Migration(6, 7) {
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

		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			for (int oldLangId = 1; oldLangId <= 9; oldLangId++) {
				database.execSQL(
					"UPDATE words " +
					" SET lang = " + getNewLanguageId(oldLangId) +
					" WHERE lang = " + oldLangId
				);
			}
		}
	};
}
