package io.github.sspanak.tt9.ime.modes.predictions;

import java.util.ArrayList;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SyllablePredictions extends Predictions {
	public SyllablePredictions(SettingsStore settings) {
		super(settings);
	}

	@Override protected boolean isRetryAllowed() { return false; }
	@Override public void onAccept(String word, String sequence) {} // not used

	@Override
	protected void onDbWords(ArrayList<String> dbWords, boolean ignored) {
		areThereDbWords = !dbWords.isEmpty();
		words.clear();
		words.addAll(dbWords);
		onWordsChanged.run();
	}
}
