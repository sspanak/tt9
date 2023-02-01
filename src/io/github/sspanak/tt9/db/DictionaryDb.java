package io.github.sspanak.tt9.db;

import android.content.Context;
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
import io.github.sspanak.tt9.preferences.SettingsStore;

public class DictionaryDb {
	private static T9RoomDb dbInstance;

	private static final RoomDatabase.Callback DROP_NORMALIZATION_TRIGGER = new RoomDatabase.Callback() {
		@Override
		public void onOpen(@NonNull SupportSQLiteDatabase db) {
			super.onOpen(db);
			db.execSQL("DROP TRIGGER IF EXISTS normalize_freq");
		}
	};


	public static synchronized void init(Context context) {
		if (dbInstance == null) {
			context = context == null ? TraditionalT9.getMainContext() : context;
			dbInstance = Room.databaseBuilder(context, T9RoomDb.class, "t9dict.db")
				.addCallback(DROP_NORMALIZATION_TRIGGER) // @todo: Remove trigger dropping after December 2023. Assuming everyone would have upgraded by then.
				.build();
		}
	}


	public static synchronized void init() {
		init(null);
	}


	private static T9RoomDb getInstance() {
		init();
		return dbInstance;
	}


	/**
	 * normalizeWordFrequencies
	 * Normalizes the word frequencies for all languages that have reached the maximum, as defined in
	 * the settings.
	 *
	 * This query will finish immediately, if there is nothing to do. It's safe to run it often.
	 *
	 */
	public static void normalizeWordFrequencies(SettingsStore settings) {
		new Thread() {
			@Override
			public void run() {
				long time = System.currentTimeMillis();

				int affectedRows = dbInstance.wordsDao().normalizeFrequencies(
					settings.getWordFrequencyNormalizationDivider(),
					settings.getWordFrequencyMax()
				);

				Logger.d(
					"db.normalizeWordFrequencies",
					"Normalized " + affectedRows + " words in: " + (System.currentTimeMillis() - time) + " ms"
				);
			}
		}.start();
	}


	public static void runInTransaction(Runnable r) {
		getInstance().runInTransaction(r);
	}


	public static void areThereWords(Handler handler, Language language) {
		new Thread() {
			@Override
			public void run() {
				int langId = language != null ? language.getId() : -1;
				handler.sendEmptyMessage(getInstance().wordsDao().count(langId) > 0 ? 1 : 0);
			}
		}.start();
	}


	public static boolean doesWordExistSync(Language language, String word) {
		if (language == null || word == null || word.equals("")) {
			return false;
		}

		return getInstance().wordsDao().doesWordExist(language.getId(), word) > 0;
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


	public static void upsertWordsSync(List<Word> words) {
		getInstance().wordsDao().upsertMany(words);
	}


	public static void incrementWordFrequency(Language language, String word, String sequence) throws Exception {
		Logger.d("incrementWordFrequency", "Incrementing priority of Word: " + word +" | Sequence: " + sequence);

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
			throw new Exception("Cannot increment word frequency. Word: " + word + " | Sequence: " + sequence);
		}

		new Thread() {
			@Override
			public void run() {
				try {
					int affectedRows = getInstance().wordsDao().incrementFrequency(language.getId(), word, sequence);

					// In case the user has changed the text case, there would be no match.
					// Try again with the lowercase equivalent.
					String lowercaseWord = "";
					if (affectedRows == 0) {
						lowercaseWord = word.toLowerCase(language.getLocale());
						affectedRows = getInstance().wordsDao().incrementFrequency(language.getId(), lowercaseWord, sequence);

						Logger.d("incrementWordFrequency", "Attempting to increment frequency for lowercase variant: " + lowercaseWord);
					}

					// Some languages permit appending the punctuation to the end of the words, like so: "try,".
					// But there are no such words in the dictionary, so try without the punctuation mark.
					if (affectedRows == 0 && language.isPunctuationPartOfWords() && sequence.endsWith("1")) {
						String truncatedWord = lowercaseWord.substring(0, word.length() - 1);
						String truncatedSequence = sequence.substring(0, sequence.length() - 1);
						affectedRows = getInstance().wordsDao().incrementFrequency(language.getId(), truncatedWord, truncatedSequence);

						Logger.d("incrementWordFrequency", "Attempting to increment frequency with stripped punctuation: " + truncatedWord);
					}

					Logger.d("incrementWordFrequency", "Affected rows: " + affectedRows);
				} catch (Exception e) {
					Logger.e(
						DictionaryDb.class.getName(),
						"Failed incrementing word frequency. Word: " + word + " | Sequence: " + sequence + ". " + e.getMessage()
					);
				}
			}
		}.start();
	}


	private static ArrayList<String> getSuggestionsExact(Language language, String sequence, String word, int maximumWords) {
		long start = System.currentTimeMillis();
		List<Word> exactMatches = getInstance().wordsDao().getMany(
			language.getId(),
			maximumWords,
			sequence,
			word == null || word.equals("") ? null : word
		);
		Logger.d(
			"db.getSuggestionsExact",
			"Exact matches: " + exactMatches.size() + ". Time: " + (System.currentTimeMillis() - start) + " ms"
		);

		ArrayList<String> suggestions = new ArrayList<>();
		for (Word w : exactMatches) {
			Logger.d("db.getSuggestions", "exact match: " + w.word + " | priority: " + w.frequency);
			suggestions.add(w.word);
		}

		return suggestions;
	}


	private static ArrayList<String> getSuggestionsFuzzy(Language language, String sequence, String word, int maximumWords) {
		long start = System.currentTimeMillis();
		List<Word> extraWords = getInstance().wordsDao().getFuzzy(
			language.getId(),
			maximumWords,
			sequence,
			word == null || word.equals("") ? null : word
		);
		Logger.d(
			"db.getSuggestionsFuzzy",
			"Fuzzy matches: " + extraWords.size() + ". Time: " + (System.currentTimeMillis() - start) + " ms"
		);

		ArrayList<String> suggestions = new ArrayList<>();
		for (Word w : extraWords) {
			Logger.d(
				"db.getSuggestions",
				"fuzzy match: " + w.word + " | sequence: " + w.sequence + " | priority: " + w.frequency
			);
			suggestions.add(w.word);
		}

		return suggestions;
	}


	private static void sendSuggestions(Handler handler, ArrayList<String> data) {
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("suggestions", data);
		Message msg = new Message();
		msg.setData(bundle);
		handler.sendMessage(msg);
	}


	public static void getSuggestions(Handler handler, Language language, String sequence, String word, int minimumWords, int maximumWords) {
		final int minWords = Math.max(minimumWords, 0);
		final int maxWords = Math.max(maximumWords, minimumWords);

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

		new Thread() {
			@Override
			public void run() {
				// get exact sequence matches, for example: "9422" -> "what"
				ArrayList<String> suggestions = getSuggestionsExact(language, sequence, word, maxWords);


				// if the exact matches are too few, add some more words that start with the same characters,
				// for example: "rol" -> "roll", "roller", "rolling", ...
				if (suggestions.size() < minWords && sequence.length() >= 2) {
					suggestions.addAll(
						getSuggestionsFuzzy(language, sequence, word, minWords - suggestions.size())
					);
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
