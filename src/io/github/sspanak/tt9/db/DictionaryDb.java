package io.github.sspanak.tt9.db;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.NotActiveException;
import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.InvalidLanguageException;

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


	private static void sendSuggestions(Handler handler, ArrayList<String> data) {
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("suggestions", data);
		Message msg = new Message();
		msg.setData(bundle);
		handler.sendMessage(msg);
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


	public static void insertWord(Context context, String word, int languageId) throws InvalidLanguageException, InsertBlankWordException, NotActiveException {
		if (languageId <= 0) {
			throw new InvalidLanguageException("Cannot insert a word for an invalid language with ID: '" + languageId + "'");
		}

		if (word == null || word.length() == 0) {
			throw new InsertBlankWordException();
		}

		// @todo: insert async with max priority for this sequence.
		throw new NotActiveException("Adding new words is disabled in this version. Please, check for updates.");
	}


	public static void insertWordsSync(Context context, List<Word> words) {
		getInstance(context).wordsDao().insertWords(words);
	}


	public static void incrementWordFrequency(Context context, int langId, String word, String sequence) throws Exception {
		if (langId <= 0) {
			throw new InvalidLanguageException("Cannot increment word frequency for an invalid language: '" + langId + "'");
		}

		// If both are empty, it is the same as changing the frequency of: "", which is simply a no-op.
		if ((word == null || word.length() == 0) && (sequence == null || sequence.length() == 0)) {
			return;
		}

		// If one of them is empty, then this is an invalid operation,
		// because a digit sequence exist for every word.
		if (word == null || word.length() == 0 || sequence == null || sequence.length() == 0) {
			throw new Exception("Cannot increment word frequency. Word: '" + word + "', Sequence: '" + sequence + "'");
		}

		new Thread() {
			@Override
			public void run() {
				try {
					getInstance(context).wordsDao().incrementFrequency(langId, word, sequence);
				} catch (Exception e) {
					Logger.e(
						DictionaryDb.class.getName(),
						"Failed incrementing word frequency. Word: '" + word + "', Sequence: '" + sequence + "'. " + e.getMessage()
					);
				}
			}
		}.start();
	}


	public static void getSuggestions(Context context, Handler handler, int langId, String sequence, int minimumWords, int maximumWords) {
		final int minWords = Math.max(minimumWords, 0);
		final int maxWords = Math.max(maximumWords, minimumWords);

		new Thread() {
			@Override
			public void run() {
				if (sequence == null || sequence.length() == 0) {
					sendSuggestions(handler, new ArrayList<>());
					return;
				}

				// get exact sequence matches, for example: "9422" -> "what"
				List<Word> exactMatches = getInstance(context).wordsDao().getMany(langId, sequence, maxWords);
				Logger.d("getWords", "Exact matches: " + exactMatches.size());

				ArrayList<String> suggestions = new ArrayList<>();
				for (Word word : exactMatches) {
					Logger.d("getWords", "exact match: " + word.word + ", priority: " + word.frequency);
					suggestions.add(word.word);
				}

				// if the exact matches are too few, add some more words that start with the same characters,
				// for example: "rol" => "roll", "roller", "rolling", ...
				if (exactMatches.size() < minWords && sequence.length() >= 2) {
					int extraWordsNeeded = minWords - exactMatches.size();
					List<Word> extraWords = getInstance(context).wordsDao().getFuzzy(langId, sequence, extraWordsNeeded);
					Logger.d("getWords", "Fuzzy matches: " + extraWords.size());

					for (Word word : extraWords) {
						Logger.d("getWords", "fuzzy match: " + word.word + ", sequence: " + word.sequence);
						suggestions.add(word.word);
					}
				}

				// pack the words in a message and send it to the calling thread
				sendSuggestions(handler, suggestions);
			}
		}.start();

	}
}
