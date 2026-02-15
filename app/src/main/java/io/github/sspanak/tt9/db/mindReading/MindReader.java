package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

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

	// @todo: move these constants to SettingsStatic
	// @todo: test and maybe set a maximum size for MindReaderNgramList? Butt beware, because this will break the connection between the dictionary IDs and the N-grams.
	private static final int MAX_NGRAM_SIZE = 4;
	private static final int MAX_BIGRAM_SUGGESTIONS = 5;
	private static final int MAX_TRIGRAM_SUGGESTIONS = 4;
	private static final int MAX_TETRAGRAM_SUGGESTIONS = 4;
	private static final int NGRAMS_INITIAL_CAPACITY = 1000;
	static final int DICTIONARY_WORD_SIZE = 16; // in bytes
	private static final int MAX_DICTIONARY_WORDS = (int) Math.pow(2, DICTIONARY_WORD_SIZE);

	// dependencies
	@Nullable private AutoTextCase autoTextCase;
	@Nullable private final ExecutorService executor;
	@Nullable private final SettingsStore settings;

	// data
	@NonNull MindReaderNgramList ngrams = new MindReaderNgramList(NGRAMS_INITIAL_CAPACITY, MAX_BIGRAM_SUGGESTIONS, MAX_TRIGRAM_SUGGESTIONS, MAX_TETRAGRAM_SUGGESTIONS);
	@NonNull MindReaderDictionary dictionary = new MindReaderDictionary(MAX_DICTIONARY_WORDS);
	private volatile int textCase = InputMode.CASE_UNDEFINED;
	@NonNull private final MindReaderContext wordContext = new MindReaderContext(MAX_NGRAM_SIZE);
	@NonNull private volatile ArrayList<String> words = new ArrayList<>();

	// loading
	private double loadingId = 0;
	@NonNull private volatile Runnable currentGuessHandler = () -> {};


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
	 * Clear the current dictionary and N-grams, as well as the timing records.
	 */
	public void clearCache() {
		dictionary = new MindReaderDictionary(wordContext.language, MAX_DICTIONARY_WORDS);
		ngrams = new MindReaderNgramList(NGRAMS_INITIAL_CAPACITY, MAX_BIGRAM_SUGGESTIONS, MAX_TRIGRAM_SUGGESTIONS, MAX_TETRAGRAM_SUGGESTIONS);
		slowestGuessCurrentTime = slowestGuessNextTime = slowestSetContextTime = slowestSetLanguageTime = 0;
	}


	/**
	 * Clear the current context and guesses. This should be called when the user finishes typing, and
	 * goes to a different app or an input field, where the current context is no longer relevant.
	 */
	public void clearContext() {
		if (!isOff() && wordContext.setText("")) {
			words.clear();
			Logger.d(LOG_TAG, "Mind reader context cleared");
		}
	}


	/**
	 * Get a copy of the current guesses, with the text case adjusted as it was set by setTextCase().
	 */
	@NonNull
	public ArrayList<String> getGuesses() {
		// @todo: when only guesses are displayed and the user presses the hardware ENTER, prevent default.
		final ArrayList<String> copy = new ArrayList<>(words);
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
	 * Given the current context and the letters matching the pressed number, get all possible next
	 * words from the dictionary.
	 */
	public void guessCurrent(double loadingId, @NonNull InputMode inputMode, @NonNull NaturalLanguage language, @NonNull String[] surroundingText, int number) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		this.loadingId = loadingId;
		final ArrayList<String> alternativeLetters = language.getKeyCharacters(number);

		if (!alternativeLetters.isEmpty() && setContextSync(inputMode, language, surroundingText, null)) {
			runInThread(() -> {
				processContext(inputMode, false);

				final Set<Integer> nextTokens = ngrams.getAllNextTokens(dictionary, wordContext);
				words = new ArrayList<>();
				for (String letter : alternativeLetters) {
					words.addAll(dictionary.getAll(nextTokens, letter));
				}

				final long time = Timer.stop(TIMER_TAG);
				slowestGuessCurrentTime = Math.max(slowestGuessCurrentTime, time);
				logState(Timer.stop(TIMER_TAG), words);

				currentGuessHandler.run();
			});
		} else {
			Timer.stop(TIMER_TAG);
		}
	}


	/**
	 * Given the current context, get all possible next words from the dictionary. This is used when
	 * the user has just typed a space, or in languages without spaces, when the user has just typed
	 * a word.
	 */
	public boolean guessNext(@NonNull InputMode inputMode, @NonNull Language language, @NonNull String[] surroundingText, @Nullable String lastWord, boolean saveContext, @NonNull Runnable onComplete) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		loadingId = 0;

		// Only attempt guessing if the context is valid and ends with a space. Otherwise, the guessed
		// composing text will appear joined with the last word, instead of as a new word.
		if (setContextSync(inputMode, language, surroundingText, lastWord) && (!language.hasSpaceBetweenWords() || TextTools.endsWithSpace(surroundingText[0]))) {
			runInThread(() -> {
				processContext(inputMode, saveContext);
				words = dictionary.getAll(ngrams.getAllNextTokens(dictionary, wordContext), null);

				final long time = Timer.stop(TIMER_TAG);
				slowestGuessNextTime = Math.max(slowestGuessNextTime, time);
				logState(time, words);

				onComplete.run();
			});
			return true;
		} else {
			Timer.stop(TIMER_TAG);
			return false;
		}
	}


	/**
	 * Set and potentially save the current context, without guessing anything.
	 */
	public void setContext(@Nullable InputMode inputMode, @NonNull Language language, @NonNull String[] surroundingText, @Nullable String lastWord) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		if (setContextSync(inputMode, language, surroundingText, lastWord)) {
			runInThread(() -> {
				processContext(inputMode, true);

				final long time = Timer.stop(TIMER_TAG);
				slowestSetContextTime = Math.max(slowestSetContextTime, time);
				logState(time, null);
			});
		} else {
			Timer.stop(TIMER_TAG);
		}
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
	public void setLanguage(@NonNull Language language) {
		if (isOff()) {
			return;
		}

		wordContext.setLanguage(language);

		if (!language.equals(wordContext.language)) {
			final String TIMER_TAG = LOG_TAG + Math.random();
			Timer.start(TIMER_TAG);

			clearContext();

			// @todo: save the current dictionary for the previous language
			// @todo: save new N-grams for this language

			// @todo: load the dictionary for the new language
			// @todo: load N-grams for the new language
			clearCache();

			final long time = Timer.stop(TIMER_TAG);
			slowestSetLanguageTime = Math.max(slowestSetLanguageTime, time);
			Logger.d(LOG_TAG, "Loaded dictionary and N-grams for language: " + language + ". Time: " + slowestSetLanguageTime + " ms");
		} else {
			Logger.d(LOG_TAG, "Keeping language: " + language);
		}
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
		processContext(inputMode, true);
	}


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


	private void processContext(@Nullable InputMode inputMode, boolean saveContext) {
		if (isOff()) {
			return;
		}

		dictionary.addAll(wordContext.language, wordContext.tokenize(dictionary));
		if (saveContext && wordContext.shouldAutoSave(inputMode)) {
			ngrams.addMany(wordContext.getAllNgrams(dictionary));
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


	private void logState(long processingTime, @Nullable ArrayList<String> words) {
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
			executor.submit(runnable);
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
		return statsSnapshot;
	}
}
