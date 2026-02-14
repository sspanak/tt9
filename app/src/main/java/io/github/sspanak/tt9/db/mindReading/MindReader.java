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
	// @todo: test and maybe set a maximum size for MindReaderNgramList?
	private static final int MAX_NGRAM_SIZE = 4;
	private static final int MAX_BIGRAM_SUGGESTIONS = 5;
	private static final int MAX_TRIGRAM_SUGGESTIONS = 4;
	private static final int MAX_TETRAGRAM_SUGGESTIONS = 4;
	private static final int NGRAMS_INITIAL_CAPACITY = 1000;
	static final int DICTIONARY_WORD_SIZE = 16; // in bytes
	private static final int MAX_DICTIONARY_WORDS = (int) Math.pow(2, DICTIONARY_WORD_SIZE);

	@Nullable private final AutoTextCase autoTextCase;
	@Nullable private final ExecutorService executor;
	@Nullable private final SettingsStore settings;

	@NonNull MindReaderNgramList ngrams = new MindReaderNgramList(NGRAMS_INITIAL_CAPACITY, MAX_BIGRAM_SUGGESTIONS, MAX_TRIGRAM_SUGGESTIONS, MAX_TETRAGRAM_SUGGESTIONS);
	@NonNull MindReaderDictionary dictionary = new MindReaderDictionary(MAX_DICTIONARY_WORDS);
	@NonNull private final MindReaderContext wordContext = new MindReaderContext(MAX_NGRAM_SIZE);
	@NonNull private volatile ArrayList<String> words = new ArrayList<>();

	private double loadingId = 0;
	@NonNull private volatile Runnable currentGuessHandler = () -> {};
	private volatile int textCase = InputMode.CASE_UNDEFINED;


	public MindReader() {
		this(null, null, null);
	}


	public MindReader(@Nullable SettingsStore settings, @Nullable ExecutorService executor, @Nullable InputType inputType) {
		this.autoTextCase = settings != null ? new AutoTextCase(settings, new Sequences(), inputType) : null;
		this.executor = executor;
		this.settings = settings;
	}


	public void clearContext() {
		// @todo: probably, always do this when the input field resets, and the language is transcribed or uses no spaces
		if (!isOff() && wordContext.setText("")) {
			words.clear();
			Logger.d(LOG_TAG, "Mind reader context cleared");
		}
	}


	@NonNull
	public ArrayList<String> getGuesses() {
		final ArrayList<String> copy = new ArrayList<>(words);
		copy.replaceAll(this::adjustWordTextCase);
		return copy;
	}


	public double getLoadingId() {
		return loadingId;
	}


	public void guessNext(@NonNull InputMode inputMode, @NonNull Language language, @NonNull String[] surroundingText, @Nullable String lastWord, boolean saveContext, @NonNull Runnable onComplete) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		loadingId = 0;

		// @todo: In Korean count space as the last word character
		// @todo: this fails for ABC. Fix it!
		if (setContextSync(inputMode, language, surroundingText, lastWord)) {
			runInThread(() -> {
				processContext(inputMode, language, saveContext);
				words = dictionary.getAll(ngrams.getAllNextTokens(dictionary, wordContext), null);

				logState(Timer.stop(TIMER_TAG), words);
				onComplete.run();
			});
		} else {
			Timer.stop(TIMER_TAG);
		}
	}


	/**
	 * Given the current context, and that the next words starts with firstLetter, guess what the word
	 * might be.
	 */
	public void guessCurrent(double loadingId, @NonNull InputMode inputMode, @NonNull NaturalLanguage language, @NonNull String[] surroundingText, int number) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		this.loadingId = loadingId;
		final ArrayList<String> alternativeLetters = language.getKeyCharacters(number);

		if (!alternativeLetters.isEmpty() && setContextSync(inputMode, language, surroundingText, null)) {
			runInThread(() -> {
				processContext(inputMode, language, false);

				final Set<Integer> nextTokens = ngrams.getAllNextTokens(dictionary, wordContext);
				words = new ArrayList<>();
				for (String letter : alternativeLetters) {
					words.addAll(dictionary.getAll(nextTokens, letter));
				}

				logState(Timer.stop(TIMER_TAG), words);
				currentGuessHandler.run();
			});
		} else {
			Timer.stop(TIMER_TAG);
		}
	}


	public void setContext(@Nullable InputMode inputMode, @NonNull Language language, @NonNull String[] surroundingText, @Nullable String lastWord) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		if (setContextSync(inputMode, language, surroundingText, lastWord)) {
			runInThread(() -> {
				processContext(inputMode, language, true);
				logState(Timer.stop(TIMER_TAG), null);
			});
		} else {
			Timer.stop(TIMER_TAG);
		}
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
				TextTools.isSingleCodePoint(lastWord)
				&& Character.isWhitespace(lastWord.codePointAt(0))
				&& wordContext.setText(surroundingText[0])
				&& wordContext.appendText(lastWord, false);
		} else if (LanguageKind.usesSpaceAsPunctuation(language) && !LanguageKind.isKorean(language)) {
			return wordContext.appendText(lastWord, true);
		} else {
			return wordContext.setText(surroundingText[0]);
		}
	}


	public MindReader setCurrentGuessHandler(@Nullable Runnable handler) {
		currentGuessHandler = handler == null ? () -> {} : handler;
		return this;
	}


	public MindReader setTextCase(int rawTextCase) {
		textCase = rawTextCase;
		return this;
	}


	public void processContext(@Nullable InputMode inputMode, @NonNull Language language, boolean saveContext) {
		if (isOff()) {
			return;
		}

		changeLanguage(language);
		dictionary.addAll(language, wordContext.tokenize(dictionary));
		if (saveContext && wordContext.shouldSave(inputMode)) {
			ngrams.addMany(wordContext.getAllNgrams(dictionary));
		}
	}


	private String adjustWordTextCase(@NonNull String word) {
		if (autoTextCase == null || (wordContext.language != null && !wordContext.language.hasUpperCase())) {
			return word;
		}

		return autoTextCase.adjustSuggestionTextCase(new Text(wordContext.language, word), textCase);
	}


	private void changeLanguage(@NonNull Language language) {
		if (!language.equals(wordContext.language)) {
			// @todo: save the current dictionary for the previous language
			// @todo: save new N-grams for this language

			// @todo: load the dictionary for the new language
			// @todo: load N-grams for the new language
			dictionary = new MindReaderDictionary(language, MAX_DICTIONARY_WORDS);
			ngrams = new MindReaderNgramList(NGRAMS_INITIAL_CAPACITY, MAX_BIGRAM_SUGGESTIONS, MAX_TRIGRAM_SUGGESTIONS, MAX_TETRAGRAM_SUGGESTIONS);
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
}
