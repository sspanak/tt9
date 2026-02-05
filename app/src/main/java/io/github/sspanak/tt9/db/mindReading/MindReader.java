package io.github.sspanak.tt9.db.mindReading;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.BaseSyncStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;

public class MindReader extends BaseSyncStore {
	private static final String LOG_TAG = MindReader.class.getSimpleName();

	// @todo: move these constants to SettingsStatic
	private static final int MAX_NGRAM_SIZE = 4;
	private static final int MAX_BIGRAM_SUGGESTIONS = 5;
	private static final int MAX_TRIGRAM_SUGGESTIONS = 4;
	private static final int MAX_TETRAGRAM_SUGGESTIONS = 4;
	private static final int NGRAMS_INITIAL_CAPACITY = 1000;
	static final int DICTIONARY_WORD_SIZE = 16; // in bytes
	private static final int MAX_DICTIONARY_WORDS = (int) Math.pow(2, DICTIONARY_WORD_SIZE);

	@NonNull private final SettingsStore settings;

	@NonNull MindReaderNgramList ngrams = new MindReaderNgramList(NGRAMS_INITIAL_CAPACITY, MAX_BIGRAM_SUGGESTIONS, MAX_TRIGRAM_SUGGESTIONS, MAX_TETRAGRAM_SUGGESTIONS);
	@NonNull MindReaderDictionary dictionary = new MindReaderDictionary(MAX_DICTIONARY_WORDS);
	@NonNull private final MindReaderContext wordContext = new MindReaderContext(dictionary, MAX_NGRAM_SIZE);


	public MindReader(@NonNull Context context, @NonNull SettingsStore settings) {
		super(context);
		this.settings = settings;
	}


	public boolean clearContext() {
		Logger.d(LOG_TAG, "Mind reader context cleared");
		return wordContext.setText("");
	}


	public boolean setContext(@NonNull String beforeCursor) {
		return isOn() && wordContext.setText(beforeCursor);
	}


	public void processContext(@NonNull Language language, boolean saveContext) {
		if (Logger.isDebugLevel()) Timer.start(LOG_TAG);

		if (!isOn()) {
			return;
		}
		changeLanguage(language);
		wordContext.parseText(); // @todo: make this case-insensitive
		if (saveContext) {
			ngrams.addMany(wordContext.getEndingNgrams());
		}

		if (Logger.isDebugLevel()) logState(Timer.stop(LOG_TAG));
	}


	public ArrayList<String> getPredictions() {
		if (!isOn()) {
			return new ArrayList<>();
		}

		final String TIMER_TAG = LOG_TAG + "predictions";
		Timer.start(TIMER_TAG);
		ArrayList<String> predictions = dictionary.getAll(ngrams.getAllNextTokens(wordContext));
		Logger.d(LOG_TAG, "Mind reader predictions retrieved in: " + Timer.stop(TIMER_TAG) + " ms");

		return predictions;
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
