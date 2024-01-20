package io.github.sspanak.tt9.db;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.exceptions.InsertBlankWordException;
import io.github.sspanak.tt9.db.sqlite.DictionaryWordBatch;
import io.github.sspanak.tt9.db.sqlite.WordStore;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class AsyncWordStore {
	private static WordStore store;
	private static final Handler asyncHandler = new Handler();

	public static synchronized void init(Context context) {
		if (store == null) {
			context = context == null ? TraditionalT9.getMainContext() : context;
			store = new WordStore(context);
		}
	}


	public static synchronized void init() {
		init(null);
	}


	private static WordStore getStore() {
		init();
		return store;
	}


	public static void runInTransaction(Runnable r) {
		try {
			getStore().beginTransaction();
			r.run();
			getStore().finishTransaction();
		} catch (Exception e) {
			getStore().failTransaction();
		}
	}


	/**
	 * normalizeWordFrequencies
	 * Normalizes the word frequencies for all languages that have reached the maximum, as defined in
	 * the settings.
	 * This query will finish immediately, if there is nothing to do. It's safe to run it often.
	 */
	public static void normalizeWordFrequencies(SettingsStore settings) {
		// @todo: work with short, not int

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
//		new Thread(() -> {
//			boolean areThere = getStore().count(language != null ? language.getId() : -1) > 0;
//			getStore().closeThreadResources();
//			notification.accept(areThere);
//		}).start();
	}


	public static void deleteWords(Context context, Runnable notification) {
		ArrayList<Integer> languageIds = new ArrayList<>();
		for (Language language : LanguageCollection.getAll(context)) {
			languageIds.add(language.getId());
		}
		deleteWords(notification, languageIds);
		// store = null;
		// init(context);
		// notification.run();
	}


	public static void deleteWords(Runnable notification, @NonNull ArrayList<Integer> languageIds) {
		new Thread(() -> {
			// @todo: run each remove in a separate thread
			for (int langId : languageIds) {
				getStore().remove(langId);
			}
			/*getStore().closeThreadResources();*/
			notification.run();
		}).start();
	}


	public static void insertWord(ConsumerCompat<Integer> statusHandler, @NonNull Language language, String word) throws Exception {
		if (word == null || word.length() == 0) {
			throw new InsertBlankWordException();
		}

//		new Thread(() -> {
//			try {
//				if (getStore().exists(language.getId(), word, language.getDigitSequenceForWord(word))) {
//					throw new UniqueViolationException("Word already exists");
//				}
//				getStore().put(Word.create(language, word, 1, true));
//				statusHandler.accept(0);
//			} catch (UniqueViolationException e) {
//				String msg = "Skipping word: '" + word + "' for language: " + language.getId() + ", because it already exists.";
//				Logger.w("insertWord", msg);
//				statusHandler.accept(1);
//			} catch (Exception e) {
//				String msg = "Failed inserting word: '" + word + "' for language: " + language.getId() + ". " + e.getMessage();
//				Logger.e("insertWord", msg);
//				statusHandler.accept(2);
//			} finally {
//				getStore().closeThreadResources();
//			}
//		}).start();
	}


	public static void upsertWordsSync(Language language, DictionaryWordBatch batch) {
//		Logger.d("upsert", "Will insert: " + batch);
		getStore().put(language, batch);
//		getStore().closeThreadResources();
	}


	public static void incrementWordFrequency(@NonNull Language language, @NonNull String word, @NonNull String sequence) {
		// If any of these is empty, it is the same as changing the frequency of: "", which is simply a no-op.
		if (word.length() == 0 || sequence.length() == 0) {
			return;
		}

//		new Thread(() -> {
//			try {
//				long start = System.currentTimeMillis();
//
//				Word dbWord = getStore().get(language.getId(), word, sequence);
//
//				// In case the user has changed the text case, there would be no match.
//				// Try again with the lowercase equivalent.
//				if (dbWord == null) {
//					dbWord = getStore().get(language.getId(), word.toLowerCase(language.getLocale()), sequence);
//				}
//
//				if (dbWord == null) {
//					throw new Exception("No such word");
//				}
//
//				int max = getStore().getMaxFrequency(dbWord.langId, dbWord.sequence, dbWord.word);
//				if (dbWord.frequency <= max) {
//					dbWord.frequency = max + 1;
//					getStore().put(dbWord);
//					long time = System.currentTimeMillis() - start;
//
//					Logger.d(
//					"incrementWordFrequency",
//					"Incremented frequency of '" + dbWord.word + "' to: " + dbWord.frequency + ". Time: " + time + " ms"
//					);
//				} else {
//					long time = System.currentTimeMillis() - start;
//					Logger.d(
//						"incrementWordFrequency",
//						"'" + dbWord.word + "' is already the top word. Keeping frequency: " + dbWord.frequency + ". Time: " + time + " ms"
//					);
//				}
//			} catch (Exception e) {
//				Logger.e(
//					DictionaryDb.class.getName(),
//					"Failed incrementing word frequency. Word: " + word + ". " + e.getMessage()
//				);
//			} finally {
//				getStore().closeThreadResources();
//			}
//		}).start();
	}


	public static void getWords(ConsumerCompat<ArrayList<String>> dataHandler, Language language, String sequence, String filter, int minimumWords, int maximumWords) {
		final int minWords = Math.max(minimumWords, 0);
		final int maxWords = Math.max(maximumWords, minWords);

		if (sequence == null || sequence.length() == 0) {
			Logger.w("db.getWords", "Attempting to get words for an empty sequence.");
			sendWords(dataHandler, new ArrayList<>());
			return;
		}

		if (language == null) {
			Logger.w("db.getWords", "Attempting to get words for NULL language.");
			sendWords(dataHandler, new ArrayList<>());
			return;
		}

		new Thread(() -> sendWords(
			dataHandler,
			getStore().getSimilar(language, sequence, filter, minWords, maxWords)
		)).start();
	}


	private static void sendWords(ConsumerCompat<ArrayList<String>> dataHandler, ArrayList<String> wordList) {
		asyncHandler.post(() -> dataHandler.accept(wordList));
	}
}
