package io.github.sspanak.tt9.ime;

import android.content.Context;

import java.util.HashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.ui.UI;

public class EmptyDatabaseWarning {
	private static final HashMap<Integer, Long> warningDisplayedTime = new HashMap<>();

	private Context context;
	private Language language;

	public EmptyDatabaseWarning() {
		for (Language lang : LanguageCollection.getAll(context)) {
			if (!warningDisplayedTime.containsKey(lang.getId())) {
				warningDisplayedTime.put(lang.getId(), 0L);
			}
		}
	}

	public void emitOnce(Language language) {
		context = context == null ? TraditionalT9.getMainContext() : context;
		this.language = language;

		if (isItTimeAgain(TraditionalT9.getMainContext())) {
			WordStoreAsync.areThereWords(this::show, language);
		}
	}

	private boolean isItTimeAgain(Context context) {
		if (this.language == null || context == null || !warningDisplayedTime.containsKey(language.getId())) {
			return false;
		}

		long now = System.currentTimeMillis();
		Long lastWarningTime = warningDisplayedTime.get(language.getId());
		return lastWarningTime != null && now - lastWarningTime > SettingsStore.DICTIONARY_MISSING_WARNING_INTERVAL;
	}

	private void show(boolean areThereWords) {
		if (areThereWords) {
			return;
		}

		warningDisplayedTime.put(language.getId(), System.currentTimeMillis());
		UI.toastLongFromAsync(
			context,
			context.getString(R.string.dictionary_missing_go_load_it, language.getName())
		);
	}
}
