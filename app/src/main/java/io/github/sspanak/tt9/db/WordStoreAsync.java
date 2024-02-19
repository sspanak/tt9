package io.github.sspanak.tt9.db;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.languages.Language;

public class WordStoreAsync {
	private static WordStore store;
	private static final Handler asyncHandler = new Handler();

	public static synchronized void init(Context context) {
		store = WordStore.getInstance(context);
	}


	public static synchronized void init() {
		init(null);
	}


	private static WordStore getStore() {
		init();
		return store;
	}


	public static void normalizeNext() {
		new Thread(() -> getStore().normalizeNext()).start();
	}


	public static void areThereWords(ConsumerCompat<Boolean> notification, Language language) {
		new Thread(() -> notification.accept(getStore().exists(language))).start();
	}

	public static void getLastLanguageUpdateTime(ConsumerCompat<Long> notification, Language language) {
		new Thread(() -> notification.accept(getStore().getLastUpdate(language))).start();
	}


	public static void deleteWords(Runnable notification, @NonNull ArrayList<Integer> languageIds) {
		new Thread(() -> {
			getStore().remove(languageIds);
			notification.run();
		}).start();
	}


	public static void put(ConsumerCompat<Integer> statusHandler, Language language, String word) {
		new Thread(() -> statusHandler.accept(getStore().put(language, word))).start();
	}


	public static void makeTopWord(@NonNull Language language, @NonNull String word, @NonNull String sequence) {
		new Thread(() -> getStore().makeTopWord(language, word, sequence)).start();
	}


	public static void getWords(ConsumerCompat<ArrayList<String>> dataHandler, Language language, String sequence, String filter, int minWords, int maxWords) {
		new Thread(() -> asyncHandler.post(() -> dataHandler.accept(
			getStore().getSimilar(language, sequence, filter, minWords, maxWords)))
		).start();
	}
}
