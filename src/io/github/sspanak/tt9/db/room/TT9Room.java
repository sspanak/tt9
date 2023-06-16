package io.github.sspanak.tt9.db.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SimpleSQLiteQuery;

import io.github.sspanak.tt9.db.migrations.DB10;
import io.github.sspanak.tt9.db.migrations.DB11;
import io.github.sspanak.tt9.db.migrations.DB6;
import io.github.sspanak.tt9.db.migrations.DB7;
import io.github.sspanak.tt9.db.migrations.DB8;
import io.github.sspanak.tt9.db.migrations.DB9;

@Database(version = 11, entities = Word.class, exportSchema = false)
public abstract class TT9Room extends RoomDatabase {
	public abstract WordsDao wordsDao();

	public static synchronized TT9Room getInstance(Context context) {
		return Room
			.databaseBuilder(context, TT9Room.class, "t9dict.db")
			.addMigrations(
				DB6.MIGRATION,
				new DB7().getMigration(context),
				DB8.MIGRATION,
				DB9.MIGRATION,
				DB10.MIGRATION,
				DB11.MIGRATION
			)
			.build();
	}

	public static SimpleSQLiteQuery getFuzzyQuery(String index, int langId, int limit, String sequence, int minWordLength, int maxWordLength, String word) {
		String sql = "SELECT *" +
			" FROM words INDEXED BY " + index + " " +
			" WHERE 1" +
			" AND lang = " + langId +
			" AND len BETWEEN " + minWordLength + " AND " + maxWordLength +
			" AND seq > " + sequence + " AND seq <= " + sequence + "99 " +
			" ORDER BY len ASC, freq DESC " +
			" LIMIT " + limit;

		if (word != null) {
			sql = sql.replace("WHERE 1", "WHERE 1 AND word LIKE '" + word.replace("'", "''") + "%'");
		}

		return new SimpleSQLiteQuery(sql);
	}

	public static SimpleSQLiteQuery getFuzzyQuery(String index, int langId, int limit, String sequence, int minWordLength, int maxWordLength) {
		return getFuzzyQuery(index, langId, limit, sequence, minWordLength, maxWordLength, null);
	}

	public static SimpleSQLiteQuery createShortWordsIndexQuery() {
		return new SimpleSQLiteQuery("CREATE INDEX IF NOT EXISTS " + WordsDao.indexShortWords + " ON words (lang ASC, len ASC, seq ASC)");
	}

	public static SimpleSQLiteQuery createLongWordsIndexQuery() {
		return new SimpleSQLiteQuery("CREATE INDEX IF NOT EXISTS " + WordsDao.indexLongWords + " ON words (lang ASC, seq ASC, freq DESC)");
	}
}
