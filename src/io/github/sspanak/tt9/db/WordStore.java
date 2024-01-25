package io.github.sspanak.tt9.db;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.entities.Word;
import io.github.sspanak.tt9.db.entities.WordList;
import io.github.sspanak.tt9.db.sqlite.DeleteOperations;
import io.github.sspanak.tt9.db.sqlite.InsertOperations;
import io.github.sspanak.tt9.db.sqlite.ReadOperations;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.db.sqlite.UpdateOperations;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.ui.AddWordAct;


public class WordStore {
	private final String LOG_TAG = "sqlite.WordStore";
	private static WordStore self;

	private SettingsStore settings;
	private SQLiteOpener sqlite = null;
	private ReadOperations readOps = null;


	public WordStore(@NonNull Context context, @NonNull SettingsStore settings) {
		try {
			this.settings = settings;
			sqlite = SQLiteOpener.getInstance(context);
			sqlite.getDb();
			readOps = new ReadOperations();
		} catch (Exception e) {
			Logger.w(LOG_TAG, "Database connection failure. All operations will return empty results. " + e.getMessage());
		}
		self = this;
	}


	public static synchronized WordStore getInstance(Context context, SettingsStore settings) {
		if (self == null) {
			context = context == null ? TraditionalT9.getMainContext() : context;
			settings = settings == null ? new SettingsStore(context) : settings;
			self = new WordStore(context, settings);
		}

		return self;
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
		String positions = SlowQueryStats.getCachedIfSlow(settings, language, sequence, filter, minWords, maxWords);
		if (positions == null) {
			positions = readOps.getSimilarWordPositions(sqlite.getDb(), language, sequence, !filter.isEmpty(), minWords);
		}
		long positionsTime = System.currentTimeMillis() - startTime;


		startTime = System.currentTimeMillis();
		ArrayList<String> words = readOps.getWords(sqlite.getDb(), language, positions, filter, maxWords, false).toStringList();
		long wordsTime = System.currentTimeMillis() - startTime;

		printLoadingSummary(sequence, words, positionsTime, wordsTime);
		SlowQueryStats.add(settings, language, sequence, filter, minWords, maxWords, (int) (positionsTime + wordsTime), positions);

		return words;
	}


	public boolean exists(Language language) {
		return language != null && checkOrNotify() && readOps.exists(sqlite.getDb(), language.getId());
	}


	public void remove(ArrayList<Integer> languageIds) {
		if (!checkOrNotify()) {
			return;
		}

		long start = System.currentTimeMillis();
		try {
			sqlite.beginTransaction();
			for (int langId : languageIds) {
				if (readOps.exists(sqlite.getDb(), langId)) {
					DeleteOperations.delete(sqlite, langId);
				}
			}
			sqlite.finishTransaction();

			Logger.d(LOG_TAG, "Deleted " + languageIds.size() + " languages. Time: " + (System.currentTimeMillis() - start) + " ms");
		} catch (Exception e) {
			sqlite.failTransaction();
			Logger.e(LOG_TAG, "Failed deleting languages. " + e.getMessage());
		}
	}


	public int put(Language language, String word) {
		if (word == null || word.isEmpty()) {
			return AddWordAct.CODE_BLANK_WORD;
		}

		if (language == null) {
			return AddWordAct.CODE_INVALID_LANGUAGE;
		}

		if (!checkOrNotify()) {
			return AddWordAct.CODE_GENERAL_ERROR;
		}

		try {
			if (readOps.exists(sqlite.getDb(), language, word)) {
				return AddWordAct.CODE_WORD_EXISTS;
			}

			String sequence = language.getDigitSequenceForWord(word);

			if (InsertOperations.addCustomWord(sqlite.getDb(), language, sequence, word)) {
				makeTopWord(language, word, sequence);
			} else {
				throw new Exception("SQLite INSERT failure.");
			}
		} catch (Exception e) {
			String msg = "Failed inserting word: '" + word + "' for language: " + language.getId() + ". " + e.getMessage();
			Logger.e("insertWord", msg);
			return AddWordAct.CODE_GENERAL_ERROR;
		}

		return AddWordAct.CODE_SUCCESS;
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
			if (!UpdateOperations.changeFrequency(sqlite.getDb(), language, wordPosition, newTopFrequency)) {
				throw new Exception("No such word");
			}

			if (newTopFrequency > settings.getWordFrequencyMax()) {
				scheduleNormalization(language);
			}

			Logger.d(LOG_TAG, "Changed frequency of '" + word + "' to: " + newTopFrequency + ". Time: " + (System.currentTimeMillis() - start) + " ms");
		} catch (Exception e) {
			Logger.e(LOG_TAG,"Frequency change failed. Word: '" + word + "'. " + e.getMessage());
		}
	}


	public void normalizeNext() {
		if (!checkOrNotify()) {
			return;
		}

		long start = System.currentTimeMillis();

		try {
			sqlite.beginTransaction();
			int nextLangId = readOps.getNextInNormalizationQueue(sqlite.getDb());
			UpdateOperations.normalize(sqlite.getDb(), settings, nextLangId);
			sqlite.finishTransaction();

			String message = nextLangId > 0 ? "Normalized language: " + nextLangId : "No languages to normalize";
			Logger.d(LOG_TAG, message + ". Time: " + (System.currentTimeMillis() - start) + " ms");
		} catch (Exception e) {
			sqlite.failTransaction();
			Logger.e(LOG_TAG, "Normalization failed. " + e.getMessage());
		}
	}


	public void scheduleNormalization(Language language) {
		if (language != null && checkOrNotify()) {
			UpdateOperations.scheduleNormalization(sqlite.getDb(), language);
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
