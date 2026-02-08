package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.Timer;

public class MindReader {
	private static final String LOG_TAG = MindReader.class.getSimpleName();

	// @todo: move these constants to SettingsStatic
	private static final int MAX_NGRAM_SIZE = 4;
	private static final int MAX_BIGRAM_SUGGESTIONS = 5;
	private static final int MAX_TRIGRAM_SUGGESTIONS = 4;
	private static final int MAX_TETRAGRAM_SUGGESTIONS = 4;
	private static final int NGRAMS_INITIAL_CAPACITY = 1000;
	static final int DICTIONARY_WORD_SIZE = 16; // in bytes
	private static final int MAX_DICTIONARY_WORDS = (int) Math.pow(2, DICTIONARY_WORD_SIZE);

	@Nullable private final ExecutorService executor;
	@Nullable private final SettingsStore settings;

	@NonNull MindReaderNgramList ngrams = new MindReaderNgramList(NGRAMS_INITIAL_CAPACITY, MAX_BIGRAM_SUGGESTIONS, MAX_TRIGRAM_SUGGESTIONS, MAX_TETRAGRAM_SUGGESTIONS);
	@NonNull MindReaderDictionary dictionary = new MindReaderDictionary(MAX_DICTIONARY_WORDS);
	@NonNull private final MindReaderContext wordContext = new MindReaderContext(MAX_NGRAM_SIZE);


	public MindReader() {
		this(null, null);
	}


	public MindReader(@Nullable SettingsStore settings, @Nullable ExecutorService executor) {
		this.executor = executor;
		this.settings = settings;
	}


	public boolean clearContext() {
		Logger.d(LOG_TAG, "Mind reader context cleared");
		return !isOff() && wordContext.setText("");
	}


	public void guessNext(@NonNull InputMode inputMode, @NonNull Language language, @NonNull String beforeCursor, @Nullable String lastWord, boolean saveContext, Consumer<ArrayList<String>> onComplete) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		// @todo: if the context contains a new line, consider only the text after the last new line
		// @todo: do not guess anything if the text after is not empty and starts with a letter (e.g. the user is in the middle of a word)
		if (setContextSync(inputMode, language, beforeCursor, lastWord)) {
			runInThread(() -> {
				processContext(inputMode, language, saveContext);
				final ArrayList<String> words = dictionary.getAll(ngrams.getAllNextTokens(dictionary, wordContext), null);

				logState(Timer.stop(TIMER_TAG), words);
				onComplete.accept(words);
			});
		}
	}


	/**
	 * Given the current context, and that the next words starts with firstLetter, guess what the word
	 * might be.
	 */
	public void guessCurrent(@NonNull InputMode inputMode, @NonNull Language language, @NonNull String beforeCursor, @NonNull String firstLetter, Consumer<ArrayList<String>> onComplete) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		if (setContextSync(inputMode, language, beforeCursor, null)) {
			runInThread(() -> {
				processContext(inputMode, language, false);
				final ArrayList<String> words = dictionary.getAll(ngrams.getAllNextTokens(dictionary, wordContext), firstLetter);

				logState(Timer.stop(TIMER_TAG), words);
				onComplete.accept(words);
			});
		}
	}


	public void setContext(@Nullable InputMode inputMode, @NonNull Language language, @NonNull String beforeCursor, @Nullable String lastWord) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		if (setContextSync(inputMode, language, beforeCursor, lastWord)) {
			runInThread(() -> {
				processContext(inputMode, language, true);
				logState(Timer.stop(TIMER_TAG), null);
			});
		}
	}


	private boolean setContextSync(@Nullable InputMode inputMode, @NonNull Language language, @NonNull String beforeCursor, @Nullable String lastWord) {
		if (isOff()) {
			return false;
		}

		if (InputModeKind.isABC(inputMode)) {
			return
				TextTools.isSingleCodePoint(lastWord)
				&& Character.isWhitespace(lastWord.codePointAt(0))
				&& wordContext.setText(beforeCursor)
				&& wordContext.appendText(lastWord, false);
		} else if (language.hasSpaceBetweenWords()) {
			return wordContext.setText(beforeCursor);
		} else {
			return wordContext.appendText(lastWord, true);
		}
	}


	public void processContext(@Nullable InputMode inputMode, @NonNull Language language, boolean saveContext) {
		if (isOff()) {
			return;
		}

		changeLanguage(language);
		dictionary.addAll(wordContext.tokenize());
		if (saveContext && wordContext.shouldSave(inputMode)) {
			ngrams.addMany(wordContext.getEndingNgrams(dictionary));
		}
	}


	private void changeLanguage(@NonNull Language language) {
		if (!language.equals(wordContext.language)) {
			// @todo: save the current dictionary for the previous language
			// @todo: save new N-grams for this language

			// @todo: load the dictionary for the new language
			// @todo: load N-grams for the new language
			dictionary = new MindReaderDictionary(language, MAX_DICTIONARY_WORDS);
		}

		wordContext.setLanguage(language);
	}


	private boolean isOff() {
		return settings == null || !settings.getAutoMindReading() || settings.isMainLayoutStealth();
	}


	private void logState(long processingTime, @Nullable ArrayList<String> words) {
		if (!Logger.isDebugLevel()) {
			return;
		}

		StringBuilder log = new StringBuilder();
		log.append("===== Mind Reading Summary =====");

		log
			.append("\ncontext: ").append(wordContext)
			.append("\nN-grams: ").append(ngrams);

		log.append("\nMagic Word Count: ").append(words != null ? words.size() : 0);

		if (processingTime >= 0) {
			log.append("\nTime: ").append(processingTime).append(" ms");
		}

		log.append('\n').append(words == null ? "No words" : words);

		Logger.d(LOG_TAG, log.toString());
	}


	private void runInThread(@NonNull Runnable runnable) {
		if (executor == null) {
			Logger.e(LOG_TAG, "MindReader can not be used without an ExecutorService");
			return;
		}

		try {
			executor.submit(runnable);
		} catch (RejectedExecutionException e) {
			Logger.e(LOG_TAG, "Failed running async MindReader task. " + e);
		}
	}
}
