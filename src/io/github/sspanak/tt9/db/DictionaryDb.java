package io.github.sspanak.tt9.db;

import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.Language;

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


	private static synchronized void createInstance() {
		dbInstance = Room.databaseBuilder(TraditionalT9.getMainContext(), T9RoomDb.class, "t9dict.db")
			.addCallback(TRIGGER_CALLBACK)
			.build();
	}


	private static T9RoomDb getInstance() {
		if (dbInstance == null) {
			createInstance();
		}

		return dbInstance;
	}


	public static void beginTransaction() {
		getInstance().beginTransaction();
	}


	public static void endTransaction(boolean success) {
		if (success) {
			getInstance().setTransactionSuccessful();
		}
		getInstance().endTransaction();
	}


	private static void sendSuggestions(Handler handler, ArrayList<String> data) {
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("suggestions", data);
		Message msg = new Message();
		msg.setData(bundle);
		handler.sendMessage(msg);
	}


	public static void truncateWords(Handler handler) {
		new Thread() {
			@Override
			public void run() {
				getInstance().clearAllTables();
				handler.sendEmptyMessage(0);
			}
		}.start();
	}


	public static void insertWord(Handler handler, Language language, String word) throws Exception {
		if (language == null) {
			throw new InvalidLanguageException();
		}

		if (word == null || word.length() == 0) {
			throw new InsertBlankWordException();
		}

		Word dbWord = new Word();
		dbWord.langId = language.getId();
		dbWord.sequence = language.getDigitSequenceForWord(word);
		dbWord.word = word.toLowerCase(language.getLocale());
		dbWord.frequency = 1;

		new Thread() {
			@Override
			public void run() {
				try {
					getInstance().wordsDao().insert(dbWord);
					getInstance().wordsDao().incrementFrequency(dbWord.langId, dbWord.word, dbWord.sequence);
					handler.sendEmptyMessage(0);
				} catch (SQLiteConstraintException e) {
					String msg = "Constraint violation when inserting a word: '" + dbWord.word + "' / sequence: '" + dbWord.sequence	+ "', for language: " + dbWord.langId;
					Logger.e("tt9/insertWord", msg);
					handler.sendEmptyMessage(1);
				} catch (Exception e) {
					String msg = "Failed inserting word: '" + dbWord.word + "' / sequence: '" + dbWord.sequence	+ "', for language: " + dbWord.langId;
					Logger.e("tt9/insertWord", msg);
					handler.sendEmptyMessage(2);
				}
			}
		}.start();
	}


	public static void insertWordsSync(List<Word> words) {
		getInstance().wordsDao().insertMany(words);
	}


	public static void incrementWordFrequency(Language language, String word, String sequence) throws Exception {
		if (language == null) {
			throw new InvalidLanguageException();
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
					getInstance().wordsDao().incrementFrequency(language.getId(), word, sequence);
				} catch (Exception e) {
					Logger.e(
						DictionaryDb.class.getName(),
						"Failed incrementing word frequency. Word: '" + word + "', Sequence: '" + sequence + "'. " + e.getMessage()
					);
				}
			}
		}.start();
	}


	public static void getSuggestions(Handler handler, Language language, String sequence, int minimumWords, int maximumWords) {
		final int minWords = Math.max(minimumWords, 0);
		final int maxWords = Math.max(maximumWords, minimumWords);

		new Thread() {
			@Override
			public void run() {
				if (sequence == null || sequence.length() == 0) {
					Logger.w("tt9/db.getSuggestions", "Attempting to get suggestions for an empty sequence.");
					sendSuggestions(handler, new ArrayList<>());
					return;
				}

				if (language == null) {
					Logger.w("tt9/db.getSuggestions", "Attempting to get suggestions for NULL language.");
					sendSuggestions(handler, new ArrayList<>());
					return;
				}

				// get exact sequence matches, for example: "9422" -> "what"
				List<Word> exactMatches = getInstance().wordsDao().getMany(language.getId(), sequence, maxWords);
				Logger.d("db.getSuggestions", "Exact matches: " + exactMatches.size());

				ArrayList<String> suggestions = new ArrayList<>();
				for (Word word : exactMatches) {
					Logger.d("db.getSuggestions", "exact match: " + word.word + ", priority: " + word.frequency);
					suggestions.add(word.word);
				}

				// if the exact matches are too few, add some more words that start with the same characters,
				// for example: "rol" => "roll", "roller", "rolling", ...
				if (exactMatches.size() < minWords && sequence.length() >= 2) {
					int extraWordsNeeded = minWords - exactMatches.size();
					List<Word> extraWords = getInstance().wordsDao().getFuzzy(language.getId(), sequence, extraWordsNeeded);
					Logger.d("db.getSuggestions", "Fuzzy matches: " + extraWords.size());

					for (Word word : extraWords) {
						Logger.d("db.getSuggestions", "fuzzy match: " + word.word + ", sequence: " + word.sequence);
						suggestions.add(word.word);
					}
				}

				if (suggestions.size() == 0) {
					Logger.i("db.getSuggestions", "No suggestions for sequence: " + sequence);
				}

				// pack the words in a message and send it to the calling thread
				sendSuggestions(handler, suggestions);
			}
		}.start();

	}
}
