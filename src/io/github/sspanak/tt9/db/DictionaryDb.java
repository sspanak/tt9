package io.github.sspanak.tt9.db;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.exceptions.InsertBlankWordException;
import io.github.sspanak.tt9.db.objectbox.Word;
import io.github.sspanak.tt9.db.objectbox.WordList;
import io.github.sspanak.tt9.db.objectbox.WordStore;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.objectbox.exception.UniqueViolationException;

public class DictionaryDb {
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


	private static void printLoadDebug(String sequence, WordList words, long startTime) {
		if (!Logger.isDebugLevel()) {
			return;
		}

		StringBuilder debugText = new StringBuilder("===== Word Matches =====");
		debugText
			.append("\n")
			.append("Word Count: ").append(words.size())
			.append(". Time: ").append(System.currentTimeMillis() - startTime).append(" ms.");
		if (words.size() > 0) {
			debugText.append("\n").append(words);
		} else {
			debugText.append(" Sequence: ").append(sequence);
		}

		Logger.d("loadWords", debugText.toString());
	}


	public static void runInTransaction(Runnable r) {
		getStore().runInTransaction(r);
	}


	/**
	 * normalizeWordFrequencies
	 * Normalizes the word frequencies for all languages that have reached the maximum, as defined in
	 * the settings.
	 * This query will finish immediately, if there is nothing to do. It's safe to run it often.
	 */
	public static void normalizeWordFrequencies(SettingsStore settings) {
		new Thread(() -> {
			for (int langId : getStore().getLanguages()) {
				getStore().runInTransactionAsync(() -> {
					long start = System.currentTimeMillis();

					if (getStore().getMaxFrequency(langId) < settings.getWordFrequencyMax()) {
						return;
					}

					List<Word> words = getStore().getMany(langId);
					if (words == null) {
						return;
					}

					for (Word w : words) {
						w.frequency /= settings.getWordFrequencyNormalizationDivider();
					}

					getStore().put(words);

					Logger.d(
						"db.normalizeWordFrequencies",
						"Normalized language: " + langId + ", " + words.size() + " words in: " + (System.currentTimeMillis() - start) + " ms"
					);
				});
			}
		}).start();
	}


	public static void areThereWords(ConsumerCompat<Boolean> notification, Language language) {
		new Thread(() -> {
			int langId = language != null ? language.getId() : -1;
			notification.accept(getStore().count(langId) > 0);
		}).start();
	}


	public static void deleteWords(Context context, Runnable notification) {
		new Thread(() -> {
			getStore().destroy();
			store = null;
			init(context);
			notification.run();
		}).start();
	}


	public static void deleteWords(Runnable notification, @NonNull ArrayList<Integer> languageIds) {
		new Thread(() -> {
			getStore().removeMany(languageIds);
			notification.run();
		}).start();
	}


	public static void insertWord(ConsumerCompat<Integer> statusHandler, @NonNull Language language, String word) throws Exception {
		if (word == null || word.length() == 0) {
			throw new InsertBlankWordException();
		}

		new Thread(() -> {
			try {
				if (getStore().exists(language.getId(), word, language.getDigitSequenceForWord(word))) {
					throw new UniqueViolationException("Word already exists");
				}
				getStore().put(Word.create(language, word, 1, true));
				statusHandler.accept(0);
			} catch (UniqueViolationException e) {
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


	public static void upsertWordsSync(List<Word> words) {
		getStore().put(words);
	}


	public static void incrementWordFrequency(@NonNull Language language, @NonNull String word, @NonNull String sequence) {
		// If any of these is empty, it is the same as changing the frequency of: "", which is simply a no-op.
		if (word.length() == 0 || sequence.length() == 0) {
			return;
		}

		new Thread(() -> {
			try {
				long start = System.currentTimeMillis();

				Word dbWord = getStore().get(language.getId(), word, sequence, true);

				// In case the user has changed the text case, there would be no match.
				// Try again with the lowercase equivalent.
				if (dbWord == null) {
					dbWord = getStore().get(language.getId(), word, sequence, false);
				}

				if (dbWord == null) {
					throw new Exception("No such word");
				}

				int max = getStore().getMaxFrequency(language.getId(), dbWord.sequence, dbWord.word);
				if (dbWord.frequency <= max) {
					dbWord.frequency = max + 1;
					getStore().put(dbWord);
					long time = System.currentTimeMillis() - start;

					Logger.d(
					"incrementWordFrequency",
					"Incremented frequency of '" + dbWord.word + "' to: " + dbWord.frequency + ". Time: " + time + " ms"
					);
				} else {
					long time = System.currentTimeMillis() - start;
					Logger.d(
						"incrementWordFrequency",
						"'" + dbWord.word + "' is already the top word. Keeping frequency: " + dbWord.frequency + ". Time: " + time + " ms"
					);
				}
			} catch (Exception e) {
				Logger.e(
					DictionaryDb.class.getName(),
					"Failed incrementing word frequency. Word: " + word + ". " + e.getMessage()
				);
			}
		}).start();
	}


	/**
	 * loadWords
	 * Loads words matching and similar to a given digit sequence
	 * For example: "7655" -> "roll" (exact match), but also: "rolled", "roller", "rolling", ...
	 * and other similar.
	 */
	private static ArrayList<String> loadWords(Language language, String sequence, String filter, int minimumWords, int maximumWords) {
		long start = System.currentTimeMillis();

		WordList matches = getStore()
			.getMany(language, sequence, filter, maximumWords)
			.filter(sequence.length(), minimumWords);

		printLoadDebug(sequence, matches, start);
		return matches.toStringList();
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
			loadWords(language, sequence, filter, minWords, maxWords))
		).start();
	}


	private static void sendWords(ConsumerCompat<ArrayList<String>> dataHandler, ArrayList<String> wordList) {
		asyncHandler.post(() -> dataHandler.accept(wordList));
	}
}
