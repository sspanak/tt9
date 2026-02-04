package io.github.sspanak.tt9.db.mindReading;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;

import io.github.sspanak.tt9.db.BaseSyncStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class MindReaderStore extends BaseSyncStore {
	private static final String LOG_TAG = MindReaderStore.class.getSimpleName();
	private static final int MAX_NGRAM_SIZE = 4;
	static final int DICTIONARY_WORD_SIZE = 16; // in bytes
	private static final int MAX_DICTIONARY_WORDS = (int) Math.pow(2, DICTIONARY_WORD_SIZE);

	@NonNull private final ExecutorService executor;
	@NonNull private final SettingsStore settings;

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
			changeLanguage(language);
			wordContext.parseText();
			wordContext.getNgrams();
			for (MindReaderNgram ngram : wordContext.getNgrams()) {
				Logger.d(LOG_TAG, "==========> Found N-gram: before=" + ngram.before + ", next=" + ngram.next);
			}
			// @todo: save new N-grams for this language
			// @todo: search for predictions using the current N-grams in wordContext
			Logger.d(LOG_TAG, "Mind reader context is now: " + wordContext);
//		});

		return true;
	}


	private void changeLanguage(@NonNull Language language) {
		if (!language.equals(wordContext.language)) {
			// @todo: save the current dictionary for the previous language
			// @todo: load the dictionary for the new language
			dictionary = new MindReaderDictionary(MAX_DICTIONARY_WORDS);
		}

		wordContext.setLanguage(language, dictionary);
	}


	private boolean isOn() {
		return settings.getAutoMindReading() && !settings.isMainLayoutStealth();
	}
}
