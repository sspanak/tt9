package io.github.sspanak.tt9.db.sqlite;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.exceptions.InsertDuplicateWordException;
import io.github.sspanak.tt9.languages.Language;


public class WordStore {
	private final String LOG_TAG = "sqlite.WordStore";
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
	}


	/**
	 * Loads words matching and similar to a given digit sequence
	 * For example: "7655" -> "roll" (exact match), but also: "rolled", "roller", "rolling", ...
	 * and other similar.
	 */
	public ArrayList<String> getSimilar(@NonNull Language language, @NonNull String sequence, @NonNull String filter, int minWords, int maxWords) {
		if (!checkOrNotify()) {
			return new ArrayList<>();
		}

		long startTime = System.currentTimeMillis();
		String positions = readOps.getWordPositions(sqlite.getDb(), language, sequence, !filter.isEmpty(), minWords);
		long positionsTime = System.currentTimeMillis() - startTime;


		startTime = System.currentTimeMillis();
		ArrayList<String> words = readOps.getWords(sqlite.getDb(), language, positions, filter, maxWords);
		long wordsTime = System.currentTimeMillis() - startTime;

		printLoadingSummary(sequence, words, positionsTime, wordsTime);

		return words;
	}

	public void remove(int languageId) {
		if (checkOrNotify()) {
			DeleteOperations.remove(sqlite, languageId);
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

		if (!CreateOperations.insertCustomWord(sqlite.getDb(), language, sequence, word)) {
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
