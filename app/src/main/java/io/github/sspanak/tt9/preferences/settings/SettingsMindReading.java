package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

public class SettingsMindReading extends SettingsPunctuation {
	SettingsMindReading(Context context) {
		super(context);
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
