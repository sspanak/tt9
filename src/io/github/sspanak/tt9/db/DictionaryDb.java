package io.github.sspanak.tt9.db;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Handler;

import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.exceptions.InsertBlankWordException;
import io.github.sspanak.tt9.db.room.TT9Room;
import io.github.sspanak.tt9.db.room.Word;
import io.github.sspanak.tt9.db.room.WordList;
import io.github.sspanak.tt9.db.room.WordsDao;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class DictionaryDb {
	private static TT9Room dbInstance;

	private static final Handler asyncHandler = new Handler();

	public static synchronized void init(Context context) {
		if (dbInstance == null) {
			context = context == null ? TraditionalT9.getMainContext() : context;
			dbInstance = TT9Room.getInstance(context);
		}
	}


	public static synchronized void init() {
		init(null);
	}


	private static TT9Room getInstance() {
		init();
		return dbInstance;
	}


	private static void printDebug(String tag, String title, String sequence, WordList words, long startTime) {
		if (!Logger.isDebugLevel()) {
			return;
		}

		StringBuilder debugText = new StringBuilder(title);
		debugText
			.append("\n")
			.append("Word Count: ").append(words.size())
			.append(". Time: ").append(System.currentTimeMillis() - startTime).append(" ms.");
		if (words.size() > 0) {
			debugText.append("\n").append(words);
		} else {
			debugText.append(" Sequence: ").append(sequence);
		}

		Logger.d(tag, debugText.toString());
	}


	public static void runInTransaction(Runnable r) {
		getInstance().runInTransaction(r);
	}


	/**
	 * normalizeWordFrequencies
	 * Normalizes the word frequencies for all languages that have reached the maximum, as defined in
	 * the settings.
	 * This query will finish immediately, if there is nothing to do. It's safe to run it often.
	 */
	public static void normalizeWordFrequencies(SettingsStore settings) {
		new Thread(() -> {
			long time = System.currentTimeMillis();

			int affectedRows = getInstance().wordsDao().normalizeFrequencies(
				settings.getWordFrequencyNormalizationDivider(),
				settings.getWordFrequencyMax()
			);

			Logger.d(
				"db.normalizeWordFrequencies",
				"Normalized " + affectedRows + " words in: " + (System.currentTimeMillis() - time) + " ms"
			);
		}).start();
	}


	public static void areThereWords(ConsumerCompat<Boolean> notification, Language language) {
		new Thread(() -> {
			int langId = language != null ? language.getId() : -1;
			notification.accept(getInstance().wordsDao().count(langId) > 0);
		}).start();
	}


	public static boolean doesWordExistSync(Language language, String word) {
		if (language == null || word == null || word.equals("")) {
			return false;
		}

		return getInstance().wordsDao().doesWordExist(language.getId(), word) > 0;
	}


	public static void deleteWords(Runnable notification) {
		deleteWords(notification, null);
	}


	public static void deleteWords(Runnable notification, ArrayList<Integer> languageIds) {
		new Thread(() -> {
			if (languageIds == null) {
				getInstance().clearAllTables();
			} else if (languageIds.size() > 0) {
				getInstance().wordsDao().deleteByLanguage(languageIds);
			}
			notification.run();
		}).start();
	}


	public static void insertWord(ConsumerCompat<Integer> statusHandler, Language language, String word) throws Exception {
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
		dbWord.length = word.length();
		dbWord.frequency = 1;

		new Thread(() -> {
			try {
				getInstance().wordsDao().insert(dbWord);
				getInstance().wordsDao().incrementFrequency(dbWord.langId, dbWord.word, dbWord.sequence);
				statusHandler.accept(0);
			} catch (SQLiteConstraintException e) {
				String msg = "Constraint violation when inserting a word: '" + dbWord.word + "' / sequence: '" + dbWord.sequence + "', for language: " + dbWord.langId
					+ ". " + e.getMessage();
				Logger.e("tt9/insertWord", msg);
				statusHandler.accept(1);
			} catch (Exception e) {
				String msg = "Failed inserting word: '" + dbWord.word + "' / sequence: '" + dbWord.sequence	+ "', for language: " + dbWord.langId + ". " + e.getMessage();
				Logger.e("tt9/insertWord", msg);
				statusHandler.accept(2);
			}
		}).start();
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

		new Thread(() -> {
			try {
				int affectedRows = getInstance().wordsDao().incrementFrequency(language.getId(), word, sequence);

				// In case the user has changed the text case, there would be no match.
				// Try again with the lowercase equivalent.
				if (affectedRows == 0) {
					String lowercaseWord = word.toLowerCase(language.getLocale());
					affectedRows = getInstance().wordsDao().incrementFrequency(language.getId(), lowercaseWord, sequence);

					Logger.d("incrementWordFrequency", "Attempting to increment frequency for lowercase variant: " + lowercaseWord);
				}

				Logger.d("incrementWordFrequency", "Affected rows: " + affectedRows);
			} catch (Exception e) {
				Logger.e(
					DictionaryDb.class.getName(),
					"Failed incrementing word frequency. Word: " + word + " | Sequence: " + sequence + ". " + e.getMessage()
				);
			}
		}).start();
	}


	/**
	 * loadWordsExact
	 * Loads words that match exactly the "sequence" and the optional "filter".
	 * For example: "7655" gets "roll".
	 */
	private static ArrayList<String> loadWordsExact(Language language, String sequence, String filter, int maximumWords) {
		long start = System.currentTimeMillis();
		WordList matches = new WordList(getInstance().wordsDao().getMany(
			language.getId(),
			maximumWords,
			sequence,
			filter == null || filter.equals("") ? null : filter
		));

		printDebug("loadWordsExact", "===== Exact Word Matches =====", sequence, matches, start);
		return matches.toStringList();
	}


	/**
	 * loadWordsFuzzy
	 * Loads words that start with "sequence" and optionally match the "filter".
	 * For example: "7655" -> "roll", but also: "rolled", "roller", "rolling", ...
	 */
	private static ArrayList<String> loadWordsFuzzy(Language language, String sequence, String filter, int maximumWords) {
		long start = System.currentTimeMillis();

		// fuzzy queries are heavy, so we must restrict the search range as much as possible
		boolean noFilter = (filter == null || filter.equals(""));
		int maxWordLength = noFilter && sequence.length() <= 2 ? 5 : 1000;
		String index = sequence.length() <= 2 ? WordsDao.indexShortWords : WordsDao.indexLongWords;
		SimpleSQLiteQuery sql = TT9Room.getFuzzyQuery(index, language.getId(), maximumWords, sequence, sequence.length(), maxWordLength, filter);

		WordList matches = new WordList(getInstance().wordsDao().getCustom(sql));

		// In some cases, searching for words starting with "digitSequence" and limited to "maxWordLength" of 5,
		// may yield too few results. If so, we expand the search range a bit.
		if (noFilter && matches.size() < maximumWords) {
			sql = TT9Room.getFuzzyQuery(
				WordsDao.indexLongWords,
				language.getId(),
				maximumWords - matches.size(),
				sequence,
				5,
				1000
			);
			matches.addAll(getInstance().wordsDao().getCustom(sql));
		}

		printDebug("loadWordsFuzzy", "~=~=~=~ Fuzzy Word Matches ~=~=~=~", sequence, matches, start);
		return matches.toStringList();
	}


	private static void sendWords(ConsumerCompat<ArrayList<String>> dataHandler, ArrayList<String> wordList) {
		asyncHandler.post(() -> dataHandler.accept(wordList));
	}


	public static void getWords(ConsumerCompat<ArrayList<String>> dataHandler, Language language, String sequence, String filter, int minimumWords, int maximumWords) {
		final int minWords = Math.max(minimumWords, 0);
		final int maxWords = Math.max(maximumWords, minimumWords);

		ArrayList<String> wordList = new ArrayList<>(maxWords);

		if (sequence == null || sequence.length() == 0) {
			Logger.w("tt9/db.getWords", "Attempting to get words for an empty sequence.");
			sendWords(dataHandler, wordList);
			return;
		}

		if (language == null) {
			Logger.w("tt9/db.getWords", "Attempting to get words for NULL language.");
			sendWords(dataHandler, wordList);
			return;
		}

		new Thread(() -> {
			wordList.addAll(loadWordsExact(language, sequence, filter, maxWords));

			if (sequence.length() > 1 && wordList.size() < minWords) {
				wordList.addAll(loadWordsFuzzy(language, sequence, filter, minWords - wordList.size()));
			}

			sendWords(dataHandler, wordList);
		}).start();
	}
}
