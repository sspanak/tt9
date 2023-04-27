package io.github.sspanak.tt9.db.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.room.TT9Room;
import io.github.sspanak.tt9.db.room.WordsDao;

public class DB9 {
	public static final Migration MIGRATION = new Migration(8, 9) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			try {
				database.beginTransaction();
				database.execSQL("DROP INDEX " + WordsDao.indexLongWords);
				database.execSQL(TT9Room.createLongWordsIndexQuery().getSql());
				database.setTransactionSuccessful();
			} catch (Exception e) {
				Logger.e("Migrate to DB9", "Migration failed. " + e.getMessage());
			} finally {
				database.endTransaction();
			}
		}
	};
}
