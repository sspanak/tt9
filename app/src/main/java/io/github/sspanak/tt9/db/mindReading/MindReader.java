package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.ime.modes.helpers.AutoTextCase;
import io.github.sspanak.tt9.ime.modes.helpers.Sequences;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.Timer;

public class MindReader {
	private static final String LOG_TAG = MindReader.class.getSimpleName();

	// dependencies
	@Nullable private AutoTextCase autoTextCase;
	@NonNull private final ExecutorService executor = Executors.newSingleThreadExecutor();
	@Nullable private SettingsStore settings;
	@NonNull public final MindReaderStats stats = new MindReaderStats(this);

	private final AtomicLong completeRequestCount = new AtomicLong(Long.MIN_VALUE);
	private final AtomicLong guessRequestCount = new AtomicLong(Long.MIN_VALUE);

	// mind-reader state
	private boolean inputNotMindReadable = false;
	@NonNull MindReaderNgramList ngrams = new MindReaderNgramList(); // worker thread only
	@NonNull MindReaderDictionary dictionary = new MindReaderDictionary(); // worker thread only
	@NonNull final MindReaderContext wordContext = new MindReaderContext(SettingsStore.MIND_READER_MAX_NGRAM_SIZE); // worker thread only

	// output (shared with main thread)
	private volatile Runnable currentGuessHandler = () -> {};
	private volatile double loadingId = 0;
	private volatile int textCase = InputMode.CASE_UNDEFINED;
	private volatile List<String> words = List.of();


	public void destroy() {
		if (!executor.isShutdown()) {
			executor.shutdownNow();
		}
	}


	/**
	 * Clear the current context and guesses. This should be called when the user finishes typing, and
	 * goes to a different app or an input field, where the current context is no longer relevant.
	 */
	public MindReader clearContext() {
		if (isOff() || executor.isTerminated() || executor.isShutdown()) {
			return this;
		}

		runInThread(this::clearContextSync);
		return this;
	}


	/**
	 * Get a copy of the current guesses, with the text case adjusted as it was set by setTextCase().
	 */
	@NonNull
	public ArrayList<String> getGuesses() {
		final List<String> snapshot = words;
		final ArrayList<String> copy = new ArrayList<>(snapshot);
		copy.replaceAll(this::adjustWordTextCase);
		return copy;
	}


	/**
	 * Get the loading ID of the current guess. This can be used to ignore outdated guesses.
	 */
	public double getLoadingId() {
		return loadingId;
	}


	/**
	 * Given the current context, get all possible completions of the words starting with the given
	 * number (e.g. if number=2, show completions starting with "a", "b", "c"). This is used when the
	 * user types the first letter of a word.
	 */
	public void complete(double loadingId, @NonNull InputMode inputMode, @NonNull NaturalLanguage language, @NonNull String[] surroundingText, int number) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		this.loadingId = loadingId;
		final long requestVersion = completeRequestCount.incrementAndGet();

		if (inputNotMindReadable) {
			Timer.stop(TIMER_TAG);
			return;
		}


		final String[] adjustedSurroundingText = MindReaderContext.handleStartOfSentenceInSurroundingText(language, surroundingText);
		final ArrayList<String> alternativeLetters = language.getKeyCharacters(number);

		if (alternativeLetters.isEmpty()) {
			Timer.stop(TIMER_TAG);
			return;
		}

		runInThread(() -> {
			if (!setContextSync(inputMode, language, adjustedSurroundingText, null)) {
				Timer.stop(TIMER_TAG);
				return;
			}

			processContext(inputMode, false);

			final Set<Integer> nextTokens = ngrams.getNextTokens(dictionary, wordContext);
			ArrayList<String> completions = new ArrayList<>();
			for (String letter : alternativeLetters) {
				completions.addAll(dictionary.getAll(nextTokens, letter));
			}

			if (requestVersion != completeRequestCount.get()) {
				Timer.stop(TIMER_TAG);
				return;
			}

			words = List.copyOf(completions);

			final long time = Timer.stop(TIMER_TAG);
			stats.recordCompleteTime(time);
			logState(time, words);

			currentGuessHandler.run();
		});
	}


	/**
	 * Given the current context, get all possible next words from the dictionary. This is used when
	 * the user has just typed a space, or in languages without spaces, when the user has just typed
	 * a word.
	 */
	public void guess(@NonNull InputMode inputMode, @NonNull Language language, @NonNull String[] surroundingText, @Nullable String lastWord, @NonNull Runnable onComplete) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		loadingId = 0; // only needed when getting the results from complete(), but we reset it for consistency
		long requestVersion = guessRequestCount.incrementAndGet();

		if (inputNotMindReadable) {
			Timer.stop(TIMER_TAG);
			return;
		}

		final String[] adjustedSurroundingText = MindReaderContext.handleStartOfSentenceInSurroundingText(language, surroundingText);

		runInThread(() -> {
			// Only attempt guessing if the context is valid and ends with a space. Otherwise, the guessed
			// composing text will appear joined with the last word, instead of as a new word.
			if (!setContextSync(inputMode, language, adjustedSurroundingText, lastWord) || (language.hasSpaceBetweenWords() && !TextTools.endsWithSpace(surroundingText[0]))) {
				Timer.stop(TIMER_TAG);
				return;
			}

			processContext(inputMode, true);

			List<String> guesses;
			if (wordContext.endsWithPunctuation()) {
				// don't be too eager to guess what comes after punctuation, emoji etc...
				guesses = new ArrayList<>();
			} else {
				guesses = dictionary.getAll(ngrams.getNextTokens(dictionary, wordContext), null);
			}

			if (requestVersion != guessRequestCount.get()) {
				Timer.stop(TIMER_TAG);
				return;
			}

			words = List.copyOf(guesses);

			final long time = Timer.stop(TIMER_TAG);
			stats.recordGuessTime(time);
			logState(time, words);

			onComplete.run();
		});
	}


	/**
	 * Clear the current dictionary and N-grams, as well as the timing records.
	 */
	@WorkerThread
	private void clearCache() {
		dictionary = new MindReaderDictionary(wordContext.language);
		ngrams = new MindReaderNgramList();
		stats.clear().update();
	}


	@WorkerThread
	private void clearContextSync() {
		if (wordContext.setText("")) {
			words = List.of();
			Logger.d(LOG_TAG, "Mind reader context cleared");
		}
	}


	/**
	 * Set and potentially save the current context, without guessing anything.
	 */
	public MindReader setContext(@Nullable InputMode inputMode, @NonNull Language language, @NonNull String[] surroundingText, @Nullable String lastWord) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		if (inputNotMindReadable) {
			Timer.stop(TIMER_TAG);
			return this;
		}

		final String[] adjustedSurroundingText = MindReaderContext.handleStartOfSentenceInSurroundingText(language, surroundingText);

		runInThread(() -> {
			if (setContextSync(inputMode, language, adjustedSurroundingText, lastWord)) {
				processContext(inputMode, true);

				final long time = Timer.stop(TIMER_TAG);
				stats.recordSetContextTime(time);
				logState(time, null);
			} else {
				Timer.stop(TIMER_TAG);
			}
		});

		return this;
	}


	/**
	 * Set a handler to be called when the current guesses are updated.
	 */
	public MindReader setCurrentGuessHandler(@Nullable Runnable handler) {
		currentGuessHandler = handler == null ? () -> {} : handler;
		return this;
	}


	public void setInputType(@Nullable InputType inputType) {
		autoTextCase = settings != null ? new AutoTextCase(settings, new Sequences(), inputType) : null;
		inputNotMindReadable = inputType == null || inputType.notMindReadableText();

		if (inputNotMindReadable) {
			Logger.d(LOG_TAG, "The current input field is not mind-readable.");
		}
	}


	/**
	 * Clear the current context and cache, and load the dictionary and N-grams for the given language.
	 */
	public MindReader setLanguage(@NonNull Language language) {
		if (isOff()) {
			return this;
		}

		runInThread(() -> {
			if (language.equals(wordContext.language)) {
				Logger.d(LOG_TAG, "Keeping language: " + language);
				return;
			}

			final String TIMER_TAG = LOG_TAG + Math.random();
			Timer.start(TIMER_TAG);

			clearContextSync();
			wordContext.setLanguage(language);

			// @todo: save the current dictionary for the previous language
			// @todo: save new N-grams for this language

			// @todo: load the dictionary for the new language
			// @todo: load N-grams for the new language
			clearCache();

			final long time = Timer.stop(TIMER_TAG);
			stats.recordSetLanguageTime(time);
			Logger.d(LOG_TAG, "Loaded dictionary and N-grams for language: " + language + ". Time: " + time + " ms");
		});

		return this;
	}


	public void setSettings(@NonNull SettingsStore settings) {
		this.settings = settings;
	}


	/**
	 * Set the preferred text case the next time getGuesses() is called.
	 */
	public MindReader setTextCase(int rawTextCase) {
		textCase = rawTextCase;
		return this;
	}


	/**
	 * Extract meaningful words from the current context and save them in the dictionary and N-grams.
	 * They can be used for guessing in the future.
	 */
	public void saveContext(@Nullable InputMode inputMode) {
		runInThread(() -> processContext(inputMode, true));
	}


	@WorkerThread
	private boolean setContextSync(@Nullable InputMode inputMode, @NonNull Language language, @NonNull String[] surroundingText, @Nullable String lastWord) {
		if (isOff()) {
			return false;
		}

		if (surroundingText.length < 2 || !surroundingText[1].isEmpty()) {
			wordContext.setText("");
			return false;
		} else if (InputModeKind.isABC(inputMode)) {
			return
				(
					(TextTools.isSingleCodePoint(lastWord) && Character.isWhitespace(lastWord.codePointAt(0)))
					|| TextTools.endsWithSpace(surroundingText[0])
				)
				&& wordContext.setText(surroundingText[0])
				&& (lastWord == null || wordContext.appendText(lastWord, false));
		} else if (LanguageKind.usesSpaceAsPunctuation(language)) {
			return wordContext.appendText(lastWord, true);
		} else {
			return wordContext.setText(surroundingText[0]);
		}
	}


	@WorkerThread
	private void processContext(@Nullable InputMode inputMode, boolean saveContext) {
		if (isOff()) {
			return;
		}

		dictionary.addAll(wordContext.language, wordContext.tokenize(dictionary));
		if (saveContext && wordContext.shouldAutoSave(inputMode)) {
			ngrams.add(wordContext.toNgram(dictionary));
		}
	}


	private String adjustWordTextCase(@NonNull String word) {
		if (autoTextCase == null || (wordContext.language != null && !wordContext.language.hasUpperCase())) {
			return word;
		}

		return autoTextCase.adjustSuggestionTextCase(new Text(wordContext.language, word), textCase);
	}


	protected boolean isOff() {
		return settings == null || !settings.getAutoMindReading() || settings.isMainLayoutStealth();
	}


	private void logThreadError(@NonNull Exception e) {
		StringBuilder errorMsg = new StringBuilder("Error in MindReader task. ");
		errorMsg.append(e.getMessage()).append("\nStack trace:");
		Arrays.stream(e.getStackTrace()).forEach(element -> errorMsg.append("\n").append(element.toString()));
		Logger.e(LOG_TAG, errorMsg.toString());
	}


	private void logState(long processingTime, @Nullable List<String> words) {
		if (!Logger.isDebugLevel()) {
			return;
		}

		StringBuilder log = new StringBuilder();
		log
			.append("===== Mind Reading Summary =====")
			.append("\ncontext: ").append(wordContext);

		if (Logger.isVerboseLevel()) {
			log
				.append("\nN-grams: ").append(ngrams)
				.append("\ndictionary: ").append(dictionary);
		}

		if (processingTime >= 0) {
			log.append("\nTime: ").append(processingTime).append(" ms");
		}

		log.append('\n').append(words == null ? "No words" : words);

		Logger.d(LOG_TAG, log.toString());
	}


	private void runInThread(@NonNull Runnable runnable) {
		if (executor.isShutdown() || executor.isTerminated()) {
			Logger.e(LOG_TAG, "Mind reading is not possible. The background thread executor is shutdown.");
			return;
		}

		try {
			executor.execute(() -> {
				try {
					runnable.run();
				} catch (Exception e) {
					logThreadError(e);
				}
			});
		} catch (RejectedExecutionException e) {
			Logger.e(LOG_TAG, "Failed running async MindReader task. " + e);
		}
	}
}
