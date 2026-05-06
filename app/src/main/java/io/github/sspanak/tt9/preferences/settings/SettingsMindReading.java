package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

public class SettingsMindReading extends SettingsPunctuation {
	SettingsMindReading(Context context) {
		super(context);
	}

	public boolean getMindReading() {
		return prefs.getBoolean("auto_mind_reading", false);
	}

	public boolean getMindReadingSortPredictionsLast() {
		return prefs.getBoolean("auto_mind_reading_sort_predictions_last", true);
	}
}
