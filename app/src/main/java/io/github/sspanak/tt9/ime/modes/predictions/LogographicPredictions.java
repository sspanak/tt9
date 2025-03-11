package io.github.sspanak.tt9.ime.modes.predictions;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class LogographicPredictions extends WordPredictions {
	public LogographicPredictions(SettingsStore settings, TextField textField) {
		super(settings, textField);
		minWords = 1;
		onlyExactMatches = true;
	}

	public void stripTranscriptions() {
		ArrayList<String> cleanWords = new ArrayList<>();

		for (int i = 0; i < words.size(); i++) {
			String word = words.get(i);
			cleanWords.add(word.replaceAll("[a-zA-Z]+", ""));
		}

		words = cleanWords;
	}

}
