package io.github.sspanak.tt9.ime.modes.predictions;

import java.util.ArrayList;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SyllablePredictions extends Predictions {
	public SyllablePredictions(SettingsStore settings) {
		super(settings);
	}

	@Override
	public void onAccept(String word, String sequence) {

	}

	@Override
	protected boolean isRetryAllowed() {
		return false;
	}

	@Override
	protected void onDbWords(ArrayList<String> dbWords, boolean retryAllowed) {

	}
}
