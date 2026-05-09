package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.languages.Language;

public class SettingsMindReading extends SettingsPunctuation {
	SettingsMindReading(Context context) {
		super(context);
	}

	public boolean areMindReaderFactoryNgramsImported(@NonNull Language language) {
		return prefs.getBoolean("mind_reader_factory_ngrams_imported_" + language.getId(), false);
	}

	public void setMindReaderFactoryNgramsImported(@NonNull Language language) {
		getPrefsEditor().putBoolean("mind_reader_factory_ngrams_imported_" + language.getId(), true).apply();
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
