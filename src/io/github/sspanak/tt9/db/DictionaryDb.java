package io.github.sspanak.tt9.db;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DictionaryDb {
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


	public static void endTransaction(Context context, boolean success) {
		if (success) {
			getInstance(context).setTransactionSuccessful();
		}
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


	public static void insertWord(Context context, String word, int languageId) throws Exception {
		// @todo: insert async with priority 1.
		throw new Exception("Adding new words is disabled in this version. Please, check for updates.");
	}


	public static void insertWordsSync(Context context, List<Word> words) {
		getInstance(context).wordsDao().insertWords(words);
	}


	public static void incrementWordFrequency(Context context, int langId, String word, String sequence) {
		new Thread() {
			@Override
			public void run() {
				getInstance(context).wordsDao().incrementFrequency(langId, word, sequence);
			}
		}.start();
	}


	public static void getSuggestions(Context context, Handler handler, int langId, String sequence, int minWords, int maxWords) {
		new Thread() {
			@Override
			public void run() {
				// get exact sequence matches, for example: "9422" -> "what"
				List<Word> exactMatches = getInstance(context).wordsDao().getMany(langId, sequence, maxWords);
				Log.d("getWords", "Exact matches: " + exactMatches.size());

				ArrayList<String> suggestions = new ArrayList<>();
				for (Word word : exactMatches) {
					Log.d("getWords", "exact match: " + word.word + " priority: " + word.frequency);
					suggestions.add(word.word);
				}

				// if the exact matches are too few, add some more words that start with the same characters,
				// for example: "rol" => "roll", "roller", "rolling", ...
				if (exactMatches.size() < minWords && sequence.length() >= 2) {
					int extraWordsNeeded = minWords - exactMatches.size();
					List<Word> extraWords = getInstance(context).wordsDao().getFuzzy(langId, sequence, extraWordsNeeded);
					for (Word word : extraWords) {
						suggestions.add(word.word);
					}

					Log.d("getWords", "Extra words: " + extraWords.size());
				}

				// pack the words in a message and send it to the calling thread
				Bundle data = new Bundle();
				data.putStringArrayList("suggestions", suggestions);
				Message msg = new Message();
				msg.setData(data);
				handler.sendMessage(msg);
			}
		}.start();

	}
}
