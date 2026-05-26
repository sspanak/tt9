package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.Language;

public class SettingsMindReading extends SettingsKeyChars {
	SettingsMindReading(Context context) {
		super(context);
	}

	public boolean areMindReaderFactoryNgramsImported(@NonNull Language language, @NonNull String newRevision) {
		final String current = prefs.getString("mind_reader_factory_ngrams_revision_" + language.getId(), "");
		return newRevision.equals(current);
	}

	public void setMindReaderFactoryNgramsRevision(@NonNull Language language, @NonNull String newRevision) {
		getPrefsEditor().putString("mind_reader_factory_ngrams_revision_" + language.getId(), newRevision).apply();
	}

	public boolean getMindReading() {
		return getMindReadingComplete() || getMindReadingGuess();
	}

	public boolean getMindReadingGuess() {
		return prefs.getBoolean("auto_mind_reading_guess", false);
	}

	public boolean getMindReadingComplete() {
		return prefs.getBoolean("auto_mind_reading_complete", false);
	}

	public boolean getMindReadingSortPredictionsLast() {
		return getMindReadingComplete() && prefs.getBoolean("auto_mind_reading_sort_predictions_last", true);
	}
}
