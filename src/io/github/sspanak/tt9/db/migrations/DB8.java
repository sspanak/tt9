package io.github.sspanak.tt9.db.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.room.TT9Room;

public class DB8 {
	public static final Migration MIGRATION = new Migration(7, 8) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			try {
				database.beginTransaction();
				database.execSQL("ALTER TABLE words ADD COLUMN len INTEGER NOT NULL DEFAULT 0");
				database.execSQL("UPDATE words SET len = LENGTH(seq)");
				database.execSQL(TT9Room.createShortWordsIndexQuery().getSql());
				database.setTransactionSuccessful();
			} catch (Exception e) {
				Logger.e("Migrate to DB8", "Migration failed. " + e.getMessage());
			} finally {
				database.endTransaction();
			}
		}
	};
}
