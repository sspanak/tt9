package io.github.sspanak.tt9.db.migrations;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DB6 {
	public static final Migration MIGRATION = new Migration(5, 6) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
			database.execSQL("DROP TRIGGER IF EXISTS normalize_freq");
		}
	};
}
