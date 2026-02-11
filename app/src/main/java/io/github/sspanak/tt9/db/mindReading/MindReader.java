package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
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
	@NonNull private volatile ArrayList<String> words = new ArrayList<>();


	public MindReader() {
		this(null, null);
	}


	public MindReader(@Nullable SettingsStore settings, @Nullable ExecutorService executor) {
		this.executor = executor;
		this.settings = settings;
	}


	public void clearContext() {
		if (!isOff() && wordContext.setText("")) {
			words.clear();
			Logger.d(LOG_TAG, "Mind reader context cleared");
		}
	}


	@NonNull
	public ArrayList<String> getCurrentWords(int textCase) {
		// @todo: use AutoTextCase.adjustParagraphTextCase() here. "before" is the context.
		final ArrayList<String> copy = new ArrayList<>(words);
		copy.replaceAll(text -> new Text(wordContext.language, text).toTextCase(textCase));
		return copy;
	}


	public void guessNext(@NonNull InputMode inputMode, @NonNull Language language, @NonNull String[] surroundingText, @Nullable String lastWord, boolean saveContext, @NonNull Runnable onComplete) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

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
	public void guessCurrent(@NonNull InputMode inputMode, @NonNull NaturalLanguage language, @NonNull String[] surroundingText, @NonNull String firstLetter, @NonNull Runnable onComplete) {
		final String TIMER_TAG = LOG_TAG + Math.random();
		Timer.start(TIMER_TAG);

		if (setContextSync(inputMode, language, surroundingText, null)) {
			runInThread(() -> {
				processContext(inputMode, language, false);

				final ArrayList<String> alternativeLetters = language.getAlternativesForLetter(firstLetter);
				if (alternativeLetters.isEmpty()) {
					alternativeLetters.add(firstLetter);
				}

				final Set<Integer> nextTokens = ngrams.getAllNextTokens(dictionary, wordContext);
				words = new ArrayList<>();
				for (String letter : alternativeLetters) {
					words.addAll(dictionary.getAll(nextTokens, letter));
				}

				logState(Timer.stop(TIMER_TAG), words);
				onComplete.run();
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
		} else if (LanguageKind.usesSpaceAsPunctuation(language)) {
			return wordContext.appendText(lastWord, true);
		} else {
			return wordContext.setText(surroundingText[0]);
		}
	}


	public void processContext(@Nullable InputMode inputMode, @NonNull Language language, boolean saveContext) {
		if (isOff()) {
			return;
		}

		changeLanguage(language);
		dictionary.addAll(wordContext.tokenize(dictionary));
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
