package io.github.sspanak.tt9.ime.modes.predictions;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class IdeogramPredictions extends WordPredictions {
	public IdeogramPredictions(SettingsStore settings, TextField textField) {
		super(settings, textField);
		minWords = 1;
		maxWords = -1;
		onlyExactMatches = true;
	}

	public void stripTranscriptions() {
		ArrayList<String> cleanWords = new ArrayList<>();

		for (int i = 0; i < words.size(); i++) {
			String cleanWord = words.get(i).replaceAll("[a-zA-Z]+", "");
			cleanWords.add(cleanWord.isEmpty() ? words.get(i) : cleanWord);
		}

		words = cleanWords;
	}

}
