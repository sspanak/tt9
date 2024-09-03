package io.github.sspanak.tt9.db;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.entities.AddWordResult;
import io.github.sspanak.tt9.db.wordPairs.WordPairStore;
import io.github.sspanak.tt9.db.words.WordStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.ConsumerCompat;

public class DataStore {
	private static final Handler asyncHandler = new Handler();
	private static WordPairStore pairs;
	private static WordStore words;


	public static void init(Context context) {
		pairs = pairs == null ? new WordPairStore(context) : pairs;
		words = words == null ? new WordStore(context) : words;
	}


	public static void destroy() {
		pairs = null;
		words = null;
	}


	public static void normalizeNext() {
		new Thread(() -> words.normalizeNext()).start();
	}


	public static void getLastLanguageUpdateTime(ConsumerCompat<String> notification, Language language) {
		new Thread(() -> notification.accept(words.getLanguageFileHash(language))).start();
	}


	public static void deleteCustomWord(Runnable notification, Language language, String word) {
		new Thread(() -> {
			words.removeCustomWord(language, word);
			notification.run();
		}).start();
	}


	public static void deleteWords(Runnable notification, @NonNull ArrayList<Integer> languageIds) {
		new Thread(() -> {
			words.remove(languageIds);
			notification.run();
		}).start();
	}


	public static void put(ConsumerCompat<AddWordResult> statusHandler, Language language, String word) {
		new Thread(() -> statusHandler.accept(words.put(language, word))).start();
	}


	public static void makeTopWord(@NonNull Language language, @NonNull String word, @NonNull String sequence) {
		new Thread(() -> words.makeTopWord(language, word, sequence)).start();
	}


	public static void getWords(ConsumerCompat<ArrayList<String>> dataHandler, Language language, String sequence, String filter, int minWords, int maxWords) {
		new Thread(() -> asyncHandler.post(() -> dataHandler.accept(
			words.getSimilar(language, sequence, filter, minWords, maxWords)))
		).start();
	}


	public static void getCustomWords(ConsumerCompat<ArrayList<String>> dataHandler, String wordFilter, int maxWords) {
		new Thread(() -> asyncHandler.post(() -> dataHandler.accept(
			words.getSimilarCustom(wordFilter, maxWords)))
		).start();
	}


	public static void countCustomWords(ConsumerCompat<Long> dataHandler) {
		new Thread(() -> asyncHandler.post(() -> dataHandler.accept(
			words.countCustom()))
		).start();
	}


	public static void exists(ConsumerCompat<ArrayList<Integer>> dataHandler, ArrayList<Language> languages) {
		new Thread(() -> asyncHandler.post(() -> dataHandler.accept(
			words.exists(languages))
		)).start();
	}


	public static void addWordPair(Language language, String word1, String word2) {
		pairs.add(language, word1, word2);
	}


	public static boolean containsWordPair(Language language, String word1, String word2) {
		return pairs.contains(language, word1, word2);
	}


	public static void saveWordPairs() {
		new Thread(() -> pairs.save()).start();
	}


	public static void loadWordPairs(ArrayList<Language> languages) {
		new Thread(() -> pairs.load(languages)).start();
	}


	public static void clearWordPairStats() {
		pairs.clear();
	}


	public static String getWordPairStats() {
		return pairs.toString();
	}
}
