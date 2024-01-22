package io.github.sspanak.tt9.db;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.entities.Word;
import io.github.sspanak.tt9.db.entities.WordList;
import io.github.sspanak.tt9.db.exceptions.InsertDuplicateWordException;
import io.github.sspanak.tt9.db.sqlite.DeleteOperations;
import io.github.sspanak.tt9.db.sqlite.InsertOperations;
import io.github.sspanak.tt9.db.sqlite.ReadOperations;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.db.sqlite.UpdateOperations;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;


public class WordStore {
	private final String LOG_TAG = "sqlite.WordStore";
	private static WordStore self;
	private SQLiteOpener sqlite = null;
	private ReadOperations readOps = null;


	public WordStore(Context context) {
		try {
			sqlite = SQLiteOpener.getInstance(context);
			sqlite.getDb();
			readOps = new ReadOperations();
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Database connection failure. All operations will return empty results. " + e.getMessage());
		}
		self = this;
	}


	public static synchronized WordStore getInstance(Context context) {
		if (self == null) {
			context = context == null ? TraditionalT9.getMainContext() : context;
			self = new WordStore(context);
		}

		return self;
	}


	public static synchronized WordStore getInstance() {
		return getInstance(null);
	}


	/**
	 * Loads words matching and similar to a given digit sequence
	 * For example: "7655" -> "roll" (exact match), but also: "rolled", "roller", "rolling", ...
	 * and other similar.
	 */
	public ArrayList<String> getSimilar(Language language, String sequence, String wordFilter, int minimumWords, int maximumWords) {
		if (!checkOrNotify()) {
			return new ArrayList<>();
		}

		if (sequence == null || sequence.length() == 0) {
			Logger.w(LOG_TAG, "Attempting to get words for an empty sequence.");
			return new ArrayList<>();
		}

		if (language == null) {
			Logger.w(LOG_TAG, "Attempting to get words for NULL language.");
			return new ArrayList<>();
		}

		final int minWords = Math.max(minimumWords, 0);
		final int maxWords = Math.max(maximumWords, minWords);
		final String filter = wordFilter == null ? "" : wordFilter;


		long startTime = System.currentTimeMillis();
		String positions = readOps.getSimilarWordPositions(sqlite.getDb(), language, sequence, !filter.isEmpty(), minWords);
		long positionsTime = System.currentTimeMillis() - startTime;


		startTime = System.currentTimeMillis();
		ArrayList<String> words = readOps.getWords(sqlite.getDb(), language, positions, filter, maxWords, false).toStringList();
		long wordsTime = System.currentTimeMillis() - startTime;

		printLoadingSummary(sequence, words, positionsTime, wordsTime);

		return words;
	}


	public boolean exists(Language language) {
		return language != null && checkOrNotify() && readOps.exists(sqlite.getDb(), language);
	}


	public void remove(ArrayList<Integer> languageId) {
		if (checkOrNotify()) {
			DeleteOperations.deleteMany(sqlite, languageId);
		}
	}


	public void put(@NonNull Language language, @NonNull String word, @NonNull String sequence) throws Exception {
		if (!checkOrNotify()) {
			return;
		}

		// @todo: check if custom words are more than 2^31-1

		if (readOps.exists(sqlite.getDb(), language, word)) {
			throw new InsertDuplicateWordException();
		}

		if (InsertOperations.insertCustomWord(sqlite.getDb(), language, sequence, word)) {
			makeTopWord(language, word, sequence);
		} else {
			throw new Exception("SQLite refused inserting the word");
		}
	}


	private boolean checkOrNotify() {
		if (sqlite == null || sqlite.getDb() == null) {
			Logger.e(LOG_TAG, "No database connection. Cannot query any data.");
			return false;
		}

		return true;
	}


	public void makeTopWord(@NonNull Language language, @NonNull String word, @NonNull String sequence) {
		if (!checkOrNotify() || word.isEmpty() || sequence.isEmpty()) {
			return;
		}

		try {
			long start = System.currentTimeMillis();

			String topWordPositions = readOps.getWordPositions(sqlite.getDb(), language, sequence, 0, 0);
			WordList topWords = readOps.getWords(sqlite.getDb(), language, topWordPositions, "", 9999, true);
			if (topWords.isEmpty()) {
				throw new Exception("No such word");
			}

			Word topWord = topWords.get(0);
			if (topWord.word.toUpperCase(language.getLocale()).equals(word.toUpperCase(language.getLocale()))) {
				Logger.d(LOG_TAG, "Word '" + word + "' is already the top word. Time: " + (System.currentTimeMillis() - start) + " ms");
				return;
			}

			int wordPosition = 0;
			for (Word tw : topWords) {
				if (tw.word.toUpperCase(language.getLocale()).equals(word.toUpperCase(language.getLocale()))) {
					wordPosition = tw.position;
					break;
				}
			}

			int newTopFrequency = readOps.getWordFrequency(sqlite.getDb(), language, topWord.position) + 1;
			if (UpdateOperations.changeFrequency(sqlite.getDb(), language, wordPosition, newTopFrequency)) {
				Logger.d(LOG_TAG, "Changed frequency of '" + word + "' to: " + newTopFrequency + ". Time: " + (System.currentTimeMillis() - start) + " ms");
			} else {
				throw new Exception("No such word");
			}
		} catch (Exception e) {
			Logger.e(LOG_TAG,"Frequency change failed. Word: '" + word + "'. " + e.getMessage());
		}
	}


	private void printLoadingSummary(String sequence, ArrayList<String> words, long positionIndexTime, long wordsTime) {
		if (!Logger.isDebugLevel()) {
			return;
		}

		StringBuilder debugText = new StringBuilder("===== Word Loading Summary =====");
		debugText
			.append("\nWord Count: ").append(words.size())
			.append(".\nTime: ").append(positionIndexTime + wordsTime)
			.append(" ms (positions: ").append(positionIndexTime)
			.append(" ms, words: ").append(wordsTime).append(" ms).");

		if (words.isEmpty()) {
			debugText.append(" Sequence: ").append(sequence);
		} else {
			debugText.append("\n").append(words);
		}

		Logger.d(LOG_TAG, debugText.toString());
	}
}
