package io.github.sspanak.tt9.db.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = 7, entities = Word.class, exportSchema = false)
public abstract class TT9Room extends RoomDatabase {
	public abstract WordsDao wordsDao();

	public static synchronized TT9Room getInstance(Context context) {
		return Room
			.databaseBuilder(context, TT9Room.class, "t9dict.db")
			.addMigrations(Migrations.v5_v6, Migrations.v6_v7)
			.build();
	}
}
