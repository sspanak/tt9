package io.github.sspanak.tt9.db;

import android.content.Context;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.github.sspanak.tt9.db.entities.AddWordResult;
import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.db.wordPairs.WordPairStore;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.db.words.WordStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class DataStore {
	private final static String LOG_TAG = DataStore.class.getSimpleName();

	private static final ExecutorService executor = Executors.newCachedThreadPool();

	private static Future<?> getWordsTask;
	private static CancellationSignal getWordsCancellationSignal = new CancellationSignal();

	private static WordPairStore pairs;
	private static WordStore words;


	public static void init(Context context) {
		words = words == null ? new WordStore(context.getApplicationContext()) : words;
		pairs = pairs == null ? new WordPairStore(context.getApplicationContext()) : pairs;
	}


	private static void runInThread(@NonNull Runnable action) {
		executor.submit(action);
	}


	private static void runInTransaction(@NonNull Runnable action, @NonNull Runnable onFinish, @NonNull String errorMessagePrefix) {
		runInThread(() -> {
			try {
				words.startTransaction();
				action.run();
				words.finishTransaction();
			} catch (Exception e) {
				words.failTransaction();
				Logger.e(LOG_TAG, errorMessagePrefix + " " + e.getMessage());
			}
			onFinish.run();
		});
	}


	public static void normalizeNext() {
		words.normalizeNext();
	}


	public static void getLastLanguageUpdateTime(Consumer<String> notification, Language language) {
		runInThread(() -> notification.accept(words.getLanguageFileHash(language)));
	}


	public static void deleteCustomWord(Runnable notification, Language language, String word) {
		runInThread(() -> {
			words.removeCustomWord(language, word);
			notification.run();
		});
	}


	public static void put(Consumer<AddWordResult> statusHandler, Language language, String word) {
		runInThread(() -> statusHandler.accept(words.put(language, word)));
	}


	public static void makeTopWord(@NonNull Language language, @NonNull String word, @NonNull String sequence) {
		runInThread(() -> words.makeTopWord(language, word, sequence));
	}


	public static void getWords(Consumer<ArrayList<String>> dataHandler, Language language, String sequence, boolean onlyExactSequence, String filter, boolean orderByLength, int minWords, int maxWords) {
		if (getWordsTask != null && !getWordsTask.isDone()) {
			getWordsCancellationSignal.cancel();
		}

		getWordsCancellationSignal = new CancellationSignal();
		getWordsTask = executor.submit(() -> getWordsSync(dataHandler, language, sequence, onlyExactSequence, filter, orderByLength, minWords, maxWords));
		executor.submit(DataStore::setGetWordsTimeout);
	}


	private static void getWordsSync(Consumer<ArrayList<String>> dataHandler, Language language, String sequence, boolean onlyExactSequence, String filter, boolean orderByLength, int minWords, int maxWords) {
		try {
			ArrayList<String> data = words.getMany(getWordsCancellationSignal, language, sequence, onlyExactSequence, filter, orderByLength, minWords, maxWords);
			dataHandler.accept(data);
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Error fetching words: " + e.getMessage());
		}
	}


	private static void setGetWordsTimeout() {
		try {
			getWordsTask.get(SettingsStore.SLOW_QUERY_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			getWordsCancellationSignal.cancel();
			Logger.e(LOG_TAG, "Word loading timed out after " + SettingsStore.SLOW_QUERY_TIMEOUT + " ms.");
		}
	}


	public static void getCustomWords(Consumer<ArrayList<CustomWord>> dataHandler, String wordFilter, int maxWords) {
		runInThread(() -> dataHandler.accept(words.getSimilarCustom(wordFilter, maxWords)));
	}


	public static void countCustomWords(Consumer<Long> dataHandler) {
		runInThread(() -> dataHandler.accept(words.countCustom()));
	}


	public static void exists(Consumer<ArrayList<Integer>> dataHandler, ArrayList<Language> languages) {
		runInThread(() -> dataHandler.accept(words.exists(languages)));
	}


	public static void addWordPair(Language language, String word1, String word2, String sequence2) {
		pairs.add(language, word1, word2, sequence2);
	}


	public static String getWord2(Language language, String word1, String sequence2) {
		return pairs.getWord2(language, word1, sequence2);
	}


	public static void saveWordPairs() {
		pairs.save();
	}


	public static void loadWordPairs(DictionaryLoader dictionaryLoader, ArrayList<Language> languages) {
		runInThread(() -> pairs.load(dictionaryLoader, languages));
	}


	public static void clearWordPairCache() {
		pairs.clearCache();
	}


	public static void deleteWordPairs(@NonNull ArrayList<Language> languages, @NonNull Runnable onDeleted) {
		runInTransaction(() -> pairs.remove(languages), onDeleted, "Failed deleting word pairs.");
	}


	public static String getWordPairStats() {
		return pairs.toString();
	}
}
