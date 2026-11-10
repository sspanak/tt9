package io.github.sspanak.tt9.db;

import android.content.Context;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.github.sspanak.tt9.db.entities.AddWordResult;
import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.db.wordPairs.WordPairStore;
import io.github.sspanak.tt9.db.words.WordStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.SupremeExecutor;

public class DataStore {
	private final static String LOG_TAG = DataStore.class.getSimpleName();

	private static ExecutorService executor;

	@Nullable private static Future<?> getWordsTask;
	@NonNull private static CancellationSignal getWordsCancellationSignal = new CancellationSignal();

	private static WordPairStore pairs;
	private static WordStore words;


	public static void init(@NonNull Context context) {
		executor = executor == null ? SupremeExecutor.get() : executor;
		pairs = pairs == null ? new WordPairStore(context.getApplicationContext()) : pairs;
		words = words == null ? new WordStore(context.getApplicationContext()) : words;
	}


	@Nullable
	private static Future<?> runInThread(@NonNull Runnable action) {
		if (executor == null || executor.isShutdown() || executor.isTerminated()) {
			Logger.e(LOG_TAG, "Cannot run a DataStore task when the executor is shutdown or NULL.");
			return null;
		}

		try {
			return executor.submit(() -> {
				try {
					action.run();
				} catch (Exception e) {
					Logger.ex(LOG_TAG, "Error in DataStore task.", e);
				}
			});
		} catch (RejectedExecutionException e) {
			Logger.e(LOG_TAG, "Failed running async DataStore task. " + e);
			return null;
		}
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


	public static void put(Consumer<AddWordResult> statusHandler, Language language, String word, boolean makeTopWord) {
		runInThread(() -> statusHandler.accept(words.put(language, word, makeTopWord)));
	}

	public static void putCustom(Consumer<AddWordResult> statusHandler, CustomWord word, boolean makeTopWord) {
		runInThread(() -> statusHandler.accept(words.put(word, makeTopWord)));
	}

	public static void replaceCustom(Consumer<AddWordResult> statusHandler, @Nullable CustomWord previous, CustomWord entry, boolean makeTopWord) {
		runInThread(() -> statusHandler.accept(words.replaceCustomWord(previous, entry, makeTopWord)));
	}

	/** Called by a prediction callback, which already runs on the database executor. */
	public static boolean isCustomWord(Language language, String word) { return words.isCustomWord(language, word); }

	/** One cancellable indexed request for a bounded set of phonetic alternatives. */
	public static void getWordVariants(Consumer<ArrayList<String>> handler, Language language, List<String> sequences, String filter, Predicate<String> accept) {
		getWordVariants(handler, language, sequences, filter, accept, null);
	}

	public static void getWordVariants(Consumer<ArrayList<String>> handler, Language language, List<String> sequences, String filter, Predicate<String> accept, @Nullable Predicate<String> acceptPrefix) {
		if (getWordsTask != null && !getWordsTask.isDone()) getWordsCancellationSignal.cancel();
		CancellationSignal cancellation = new CancellationSignal();
		getWordsCancellationSignal = cancellation;
		List<String> snapshot = new ArrayList<>(sequences);
		getWordsTask = runInThread(() -> {
			LinkedHashSet<String> result = new LinkedHashSet<>();
			for (int i = 0; i < snapshot.size() && !cancellation.isCanceled(); i++) {
				ArrayList<String> matches = words.getMany(cancellation, language, snapshot.get(i), true, filter, false, 1, SettingsStore.SUGGESTIONS_MAX);
				for (String word : matches) if (accept.test(word)) result.add(word);
			}
			// An unfinished syllable can have no exact row while still being a valid word prefix.
			if (result.isEmpty() && acceptPrefix != null && !snapshot.isEmpty() && !cancellation.isCanceled()) {
				for (String word : words.getMany(cancellation, language, snapshot.get(0), false, filter, false, 1, SettingsStore.SUGGESTIONS_MAX)) {
					if (acceptPrefix.test(word)) result.add(word);
				}
			}
			if (!cancellation.isCanceled()) handler.accept(new ArrayList<>(result));
		});
		Future<?> task = getWordsTask;
		runInThread(() -> {
			try { if (task != null) task.get(SettingsStore.SLOW_QUERY_TIMEOUT, TimeUnit.MILLISECONDS); }
			catch (Exception e) { cancellation.cancel(); }
		});
	}


	public static void makeTopWord(@NonNull Language language, @NonNull String word, @NonNull String sequence) {
		runInThread(() -> words.makeTopWord(language, word, sequence));
	}


	public static void getWords(Consumer<ArrayList<String>> dataHandler, Language language, String sequence, boolean onlyExactSequence, String filter, boolean orderByLength, int minWords, int maxWords) {
		if (getWordsTask != null && !getWordsTask.isDone()) {
			getWordsCancellationSignal.cancel();
		}

		getWordsCancellationSignal = new CancellationSignal();
		getWordsTask = runInThread(() -> getWordsSync(dataHandler, language, sequence, onlyExactSequence, filter, orderByLength, minWords, maxWords));
		runInThread(DataStore::setGetWordsTimeout);
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
		if (getWordsTask == null) {
			return;
		}

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


	@Nullable
	public static String getWord(@NonNull Language language, @NonNull String word, @NonNull String sequence) {
		return words.getWord(language, word, sequence);
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


	public static void loadWordPairs(ArrayList<Language> languages) {
		runInThread(() -> pairs.load(languages));
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
