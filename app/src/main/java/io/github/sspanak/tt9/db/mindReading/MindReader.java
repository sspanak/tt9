package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
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
	@Nullable private final ExecutorService executor;
	@Nullable private final SettingsStore settings;

	private final AtomicLong completeRequestCount = new AtomicLong(Long.MIN_VALUE);
	private final AtomicLong guessRequestCount = new AtomicLong(Long.MIN_VALUE);

	// mind-reader state (worker thread only)
	@NonNull private MindReaderNgramList ngrams = new MindReaderNgramList();
	@NonNull private MindReaderDictionary dictionary = new MindReaderDictionary();
	@NonNull private final MindReaderContext wordContext = new MindReaderContext(SettingsStore.MIND_READER_MAX_NGRAM_SIZE);

	// output (shared with main thread)
	private volatile Runnable currentGuessHandler = () -> {};
	private volatile double loadingId = 0;
	private volatile int textCase = InputMode.CASE_UNDEFINED;
	private volatile List<String> words = List.of();

	// statistics
	private static long slowestGuessCurrentTime = 0;
	private static long slowestGuessNextTime = 0;
	private static long slowestSetContextTime = 0;
	private static long slowestSetLanguageTime = 0;
	@NonNull private static String statsSnapshot = "";


	public MindReader() {
		this(null, null);
	}


	public MindReader(@Nullable SettingsStore settings, @Nullable ExecutorService executor) {
		this.executor = executor;
		this.settings = settings;
		updateStats();
	}


	/**
	 * Clear the current context and guesses. This should be called when the user finishes typing, and
	 * goes to a different app or an input field, where the current context is no longer relevant.
	 */
	public void clearContext() {
		if (!isOff()) {
			runInThread(this::clearContextSync);
		}
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
	public void complete(double loadingId, @NonNull InputType inputType, @NonNull InputMode inputMode, @NonNull NaturalLanguage language, @NonNull String[] surroundingText, int number) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		this.loadingId = loadingId;
		final long requestVersion = completeRequestCount.incrementAndGet();

		if (inputType.notMindReadableText()) {
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
			slowestGuessCurrentTime = Math.max(slowestGuessCurrentTime, time);
			logState(time, words);

			currentGuessHandler.run();
		});
	}


	/**
	 * Given the current context, get all possible next words from the dictionary. This is used when
	 * the user has just typed a space, or in languages without spaces, when the user has just typed
	 * a word.
	 */
	public void guess(@NonNull InputType inputType, @NonNull InputMode inputMode, @NonNull Language language, @NonNull String[] surroundingText, @Nullable String lastWord, @NonNull Runnable onComplete) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		loadingId = 0; // only needed when getting the results from complete(), but we reset it for consistency
		long requestVersion = guessRequestCount.incrementAndGet();

		if (inputType.notMindReadableText()) {
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
			slowestGuessNextTime = Math.max(slowestGuessNextTime, time);
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
		slowestGuessCurrentTime = slowestGuessNextTime = slowestSetContextTime = slowestSetLanguageTime = 0;
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

		final String[] adjustedSurroundingText = MindReaderContext.handleStartOfSentenceInSurroundingText(language, surroundingText);

		runInThread(() -> {
			if (setContextSync(inputMode, language, adjustedSurroundingText, lastWord)) {
				processContext(inputMode, true);

				final long time = Timer.stop(TIMER_TAG);
				slowestSetContextTime = Math.max(slowestSetContextTime, time);
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
			slowestSetLanguageTime = Math.max(slowestSetLanguageTime, time);
			Logger.d(LOG_TAG, "Loaded dictionary and N-grams for language: " + language + ". Time: " + slowestSetLanguageTime + " ms");
		});

		return this;
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


	private boolean isOff() {
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
		log.append("===== Mind Reading Summary =====");

		log
			.append("\ncontext: ").append(wordContext)
			.append("\nN-grams: ").append(ngrams)
			.append("\ndictionary: ").append(dictionary);

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


	public MindReader updateStats() {
		StringBuilder sb = new StringBuilder();

		sb.append("Status").append(isOff() ? ": Off\n" : ": On\n");
		sb.append("Language: ").append(wordContext.language).append("\n");
		sb.append("Dictionary size: ").append(dictionary.size()).append(" tokens\n");
		sb.append("N-grams: ").append(ngrams.size()).append(" / ").append(ngrams.capacity()).append("\n");

		if (!isOff()) {
			sb.append("\nSlowest guess-current time: ").append(slowestGuessCurrentTime).append(" ms\n");
			sb.append("Slowest guess-next time: ").append(slowestGuessNextTime).append(" ms\n");
			sb.append("Slowest set-context time: ").append(slowestSetContextTime).append(" ms\n");
			sb.append("Slowest set-language time: ").append(slowestSetLanguageTime).append(" ms\n");
		}

		statsSnapshot = sb.toString();

		return this;
	}


	@NonNull
	public static String getStats() {
		return statsSnapshot.isEmpty() ? "No mind read." : statsSnapshot;
	}
}
