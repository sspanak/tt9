package io.github.sspanak.tt9.db;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;

public class T9Database {
	private static T9RoomDb dbInstance;


	private static final RoomDatabase.Callback TRIGGER_CALLBACK = new RoomDatabase.Callback() {
		@Override
		public void onCreate(@NonNull SupportSQLiteDatabase db) {
			super.onCreate(db);
			db.execSQL(
				"CREATE TRIGGER IF NOT EXISTS normalize_freq " +
				" AFTER UPDATE ON words " +
				" WHEN NEW.freq > 50000 " +
				" BEGIN" +
					" UPDATE words SET freq = freq / 10000 " +
					" WHERE seq = NEW.seq; " +
				"END;"
			);
		}

		@Override
		public void onOpen(@NonNull SupportSQLiteDatabase db) {
			super.onOpen(db);
		}
	};


	private static synchronized void createInstance(Context context) {
		dbInstance = Room.databaseBuilder(context, T9RoomDb.class, "t9dict.db")
			.addCallback(TRIGGER_CALLBACK)
			.build();
	}


	public static T9RoomDb getInstance(Context context) {
		if (dbInstance == null) {
			createInstance(context);
		}

		return dbInstance;
	}


	public static void beginTransaction(Context context) {
		getInstance(context).beginTransaction();
	}


	public static void endTransaction(Context context) {
		getInstance(context).endTransaction();
	}


	public static void truncateWords(Context context, Handler handler) {
		new Thread() {
			@Override
			public void run() {
				getInstance(context).clearAllTables();
				handler.sendEmptyMessage(0);
			}
		}.start();
	}


	public static void insertWordsSync(Context context, List<Word> words) {
		getInstance(context).wordsDao().insertWords(words);
	}
}
