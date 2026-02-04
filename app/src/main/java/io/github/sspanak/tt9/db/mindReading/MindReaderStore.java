package io.github.sspanak.tt9.db.mindReading;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;

import io.github.sspanak.tt9.db.BaseSyncStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;

public class MindReaderStore extends BaseSyncStore {
	private static final String LOG_TAG = MindReaderStore.class.getSimpleName();
	private static final int MAX_NGRAM_SIZE = 4;
	private static final int NGRAMS_INITIAL_CAPACITY = 1000;
	static final int DICTIONARY_WORD_SIZE = 16; // in bytes
	private static final int MAX_DICTIONARY_WORDS = (int) Math.pow(2, DICTIONARY_WORD_SIZE);

	@NonNull private final ExecutorService executor;
	@NonNull private final SettingsStore settings;

	@NonNull MindReaderNgramList ngrams = new MindReaderNgramList(NGRAMS_INITIAL_CAPACITY);
	@NonNull MindReaderDictionary dictionary = new MindReaderDictionary(MAX_DICTIONARY_WORDS);
	@NonNull private final MindReaderContext wordContext = new MindReaderContext(dictionary, MAX_NGRAM_SIZE);


	public MindReaderStore(@NonNull Context context, @NonNull ExecutorService executor, @NonNull SettingsStore settings) {
		super(context);
		this.executor = executor;
		this.settings = settings;
	}


	public boolean clearContext() {
		Logger.d(LOG_TAG, "Mind reader context cleared");
		return wordContext.setText(null);
	}


	public boolean setContext(@NonNull Language language, @Nullable String beforeCursor) {
		if (!isOn() || !wordContext.setText(beforeCursor)) {
			return false;
		}

//		executor.submit(() -> {
			if (Logger.isDebugLevel()) Timer.start(LOG_TAG);

			changeLanguage(language);
			wordContext.parseText();
			wordContext.getNgrams();
			ngrams.addMany(wordContext.getNgrams());

			// @todo: search for predictions using the current N-grams in wordContext

			if (Logger.isDebugLevel()) logState(Timer.stop(LOG_TAG));
//		});

		return true;
	}


	private void changeLanguage(@NonNull Language language) {
		if (!language.equals(wordContext.language)) {
			// @todo: save the current dictionary for the previous language
			// @todo: save new N-grams for this language

			// @todo: load the dictionary for the new language
			dictionary = new MindReaderDictionary(MAX_DICTIONARY_WORDS);
		}

		wordContext.setLanguage(language, dictionary);
	}


	private boolean isOn() {
		return settings.getAutoMindReading() && !settings.isMainLayoutStealth();
	}


	private void logState(long processingTime) {
		Logger.d(LOG_TAG, "Mind reader context: " + wordContext);
		Logger.d(LOG_TAG, "Mind reader N-grams: " + ngrams);
		if (processingTime >= 0) {
			Logger.d(LOG_TAG, "Mind reader context processed in: " + processingTime + " ms");
		}
	}
}
