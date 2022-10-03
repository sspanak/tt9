package io.github.sspanak.tt9.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(version = 5, entities = Word.class, exportSchema = false)
abstract class T9RoomDb extends RoomDatabase {
	public abstract WordsDao wordsDao();
}
