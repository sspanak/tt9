package io.github.sspanak.tt9.db;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.exceptions.InsertBlankWordException;
import io.github.sspanak.tt9.db.exceptions.InsertDuplicateWordException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class AsyncWordStore {
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


	/**
	 * normalizeWordFrequencies
	 * Normalizes the word frequencies for all languages that have reached the maximum, as defined in
	 * the settings.
	 * This query will finish immediately, if there is nothing to do. It's safe to run it often.
	 */
	public static void normalizeWordFrequencies(SettingsStore settings) {
//		final String LOG_TAG = "db.normalizeWordFrequencies";
//
//		new Thread(() -> {
//			for (int langId : getStore().getLanguages()) {
//				getStore().runInTransactionAsync(() -> {
//					try {
//						long start = System.currentTimeMillis();
//
//						if (getStore().getMaxFrequency(langId) < settings.getWordFrequencyMax()) {
//							return;
//						}
//
//						List<Word> words = getStore().getMany(langId);
//						if (words == null) {
//							return;
//						}
//
//						for (Word w : words) {
//							w.frequency /= settings.getWordFrequencyNormalizationDivider();
//						}
//
//						getStore().put(words);
//
//						Logger.d(
//							LOG_TAG,
//							"Normalized language: " + langId + ", " + words.size() + " words in: " + (System.currentTimeMillis() - start) + " ms"
//						);
//					} catch (Exception e) {
//						Logger.e(LOG_TAG, "Word normalization failed. " + e.getMessage());
//					} finally {
//						getStore().closeThreadResources();
//					}
//				});
//			}
//		}).start();
	}


	public static void areThereWords(ConsumerCompat<Boolean> notification, Language language) {
		new Thread(() -> notification.accept(getStore().exists(language))).start();
	}


	public static void deleteWords(Runnable notification, @NonNull ArrayList<Integer> languageIds) {
		new Thread(() -> {
			getStore().remove(languageIds);
			notification.run();
		}).start();
	}


	public static void insertWord(ConsumerCompat<Integer> statusHandler, @NonNull Language language, String word) throws Exception {
		// @todo: migrate all logic to the WordStore
		if (word == null || word.length() == 0) {
			throw new InsertBlankWordException();
		}

		new Thread(() -> {
			try {
				getStore().put(language, word, language.getDigitSequenceForWord(word));
				statusHandler.accept(0);
			} catch (InsertDuplicateWordException e) {
				String msg = "Skipping word: '" + word + "' for language: " + language.getId() + ", because it already exists.";
				Logger.w("insertWord", msg);
				statusHandler.accept(1);
			} catch (Exception e) {
				String msg = "Failed inserting word: '" + word + "' for language: " + language.getId() + ". " + e.getMessage();
				Logger.e("insertWord", msg);
				statusHandler.accept(2);
			}
		}).start();
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
