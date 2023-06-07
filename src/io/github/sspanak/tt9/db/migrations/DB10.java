package io.github.sspanak.tt9.db.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.room.TT9Room;

public class DB10 {
	public static final Migration MIGRATION = new Migration(9, 10) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			try {
				database.beginTransaction();
				database.execSQL(TT9Room.createShortWordsIndexQuery().getSql());
				database.execSQL(TT9Room.createLongWordsIndexQuery().getSql());
				database.setTransactionSuccessful();
			} catch (Exception e) {
				Logger.e("Migrate to DB10", "Migration failed. " + e.getMessage());
			} finally {
				database.endTransaction();
			}
		}
	};
}
