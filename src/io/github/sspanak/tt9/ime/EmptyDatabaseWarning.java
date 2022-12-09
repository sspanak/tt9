package io.github.sspanak.tt9.ime;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.HashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.ui.UI;

public class EmptyDatabaseWarning {
	final int WARNING_INTERVAL;
	private static final HashMap<Integer, Long> warningDisplayedTime = new HashMap<>();

	private Language language;

	public EmptyDatabaseWarning(SettingsStore settings) {
		WARNING_INTERVAL = settings.getDictionaryMissingWarningInterval();

		for (Language lang : LanguageCollection.getAll()) {
			if (!warningDisplayedTime.containsKey(lang.getId())) {
				warningDisplayedTime.put(lang.getId(), 0L);
			}
		}
	}

	public void emitOnce(Language language) {
		if (language == null) {
			return;
		}

		this.language = language;
		DictionaryDb.areThereWords(handleWordCount, language);
	}

	private final Handler handleWordCount = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			boolean areThereWords = msg.what == 1;

			if (areThereWords) {
				return;
			}

			Context context = TraditionalT9.getMainContext();
			if (context == null || !warningDisplayedTime.containsKey(language.getId())) {
				return;
			}

			long now = System.currentTimeMillis();
			Long lastWarningTime = warningDisplayedTime.get(language.getId());
			boolean isItWarningTimeAgain = lastWarningTime != null && now - lastWarningTime > WARNING_INTERVAL;

			if (isItWarningTimeAgain) {
				String message = context.getString(R.string.dictionary_missing_go_load_it, language.getName());
				UI.toastLong(context, message);
				warningDisplayedTime.put(language.getId(), now);
			}
		}
	};
}
