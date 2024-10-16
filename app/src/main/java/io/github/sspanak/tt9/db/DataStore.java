package io.github.sspanak.tt9.db;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.entities.AddWordResult;
import io.github.sspanak.tt9.db.wordPairs.WordPairStore;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.db.words.WordStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

public class DataStore {
	private static final Handler asyncHandler = new Handler();
	private static WordPairStore pairs;
	private static WordStore words;


	public static void init(Context context) {
		words = words == null ? new WordStore(context.getApplicationContext()) : words;
		pairs = pairs == null ? new WordPairStore(context.getApplicationContext()) : pairs;
	}


	private static void runInThread(@NonNull Runnable action) {
		new Thread(action).start();
	}


	private static void runInTransaction(@NonNull Runnable action, @NonNull Runnable onFinish, @NonNull String errorMessagePrefix) {
		runInThread(() -> {
			try {
				words.startTransaction();
				action.run();
				words.finishTransaction();
			} catch (Exception e) {
				words.failTransaction();
				Logger.e(DataStore.class.getSimpleName(), errorMessagePrefix + " " + e.getMessage());
			}
			onFinish.run();
		});
	}


	public static void normalizeNext() {
		runInThread(() -> words.normalizeNext());
	}


	public static void getLastLanguageUpdateTime(ConsumerCompat<String> notification, Language language) {
		runInThread(() -> notification.accept(words.getLanguageFileHash(language)));
	}


	public static void deleteCustomWord(Runnable notification, Language language, String word) {
		runInThread(() -> {
			words.removeCustomWord(language, word);
			notification.run();
		});
	}


	public static void deleteLanguages(Runnable notification, @NonNull ArrayList<Language> languages) {
		runInTransaction(
			() -> { words.remove(languages); pairs.remove(languages); },
			notification,
			"Failed deleting languages."
		);
	}


	public static void put(ConsumerCompat<AddWordResult> statusHandler, Language language, String word) {
		runInThread(() -> statusHandler.accept(words.put(language, word)));
	}


	public static void makeTopWord(@NonNull Language language, @NonNull String word, @NonNull String sequence) {
		runInThread(() -> words.makeTopWord(language, word, sequence));
	}


	public static void getWords(ConsumerCompat<ArrayList<String>> dataHandler, Language language, String sequence, String filter, int minWords, int maxWords) {
		runInThread(() -> {
			ArrayList<String> data = words.getSimilar(language, sequence, filter, minWords, maxWords);
			asyncHandler.post(() -> dataHandler.accept(data));
		});
	}


	public static void getCustomWords(ConsumerCompat<ArrayList<String>> dataHandler, String wordFilter, int maxWords) {
		runInThread(() -> {
			ArrayList<String> data = words.getSimilarCustom(wordFilter, maxWords);
			asyncHandler.post(() -> dataHandler.accept(data));
		});
	}


	public static void countCustomWords(ConsumerCompat<Long> dataHandler) {
		runInThread(() -> {
			long data = words.countCustom();
			asyncHandler.post(() -> dataHandler.accept(data));
		});
	}


	public static void exists(ConsumerCompat<ArrayList<Integer>> dataHandler, ArrayList<Language> languages) {
		runInThread(() -> {
			ArrayList<Integer> data = words.exists(languages);
			asyncHandler.post(() -> dataHandler.accept(data));
		});
	}


	public static void addWordPair(Language language, String word1, String word2, String sequence2) {
		pairs.add(language, word1, word2, sequence2);
	}


	public static String getWord2(Language language, String word1, String sequence2) {
		return pairs.getWord2(language, word1, sequence2);
	}


	public static void saveWordPairs() {
		runInThread(() -> pairs.save());
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
