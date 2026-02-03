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

	private static final int MAX_TOKENS = 4;
	private static final int MAX_DICTIONARY_WORDS = 65536;


	@NonNull private final ExecutorService executor;
	@NonNull private final SettingsStore settings;


	@NonNull MindReaderDictionary dictionary = new MindReaderDictionary(MAX_DICTIONARY_WORDS);
	@NonNull private final MindReaderContext wordContext = new MindReaderContext(dictionary, MAX_TOKENS);


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
			wordContext.process();
			Logger.d(LOG_TAG, "Mind reader context is now: " + wordContext);
//		});

		return true;
	}


	private void changeLanguage(@NonNull Language language) {
		if (!language.equals(wordContext.language)) {
			// @todo: save the current dictionary
			// @todo: load the dictionary for the new language
			dictionary = new MindReaderDictionary(MAX_DICTIONARY_WORDS);
		}

		wordContext.setLanguage(language, dictionary);
	}



	private boolean isOn() {
		return settings.getAutoMindReading() && !settings.isMainLayoutStealth();
	}
}
