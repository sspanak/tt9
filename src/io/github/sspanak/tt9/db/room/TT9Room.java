package io.github.sspanak.tt9.db.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import io.github.sspanak.tt9.db.migrations.DB6;
import io.github.sspanak.tt9.db.migrations.DB7;

@Database(version = 7, entities = Word.class, exportSchema = false)
public abstract class TT9Room extends RoomDatabase {
	public abstract WordsDao wordsDao();

	public static synchronized TT9Room getInstance(Context context) {
		return Room
			.databaseBuilder(context, TT9Room.class, "t9dict.db")
			.addMigrations(DB6.MIGRATION, new DB7().getMigration(context))
			.build();
	}
}
