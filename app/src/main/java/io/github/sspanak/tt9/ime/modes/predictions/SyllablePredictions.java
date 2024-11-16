package io.github.sspanak.tt9.ime.modes.predictions;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.modes.helpers.Cheonjiin;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SyllablePredictions extends Predictions {
	int defaultMinWords;
	int loadAttempts;
	String lastWord = "";
	String lastStableWord = "";
	int lastStableSequenceLength;

	public SyllablePredictions(SettingsStore settings) {
		super(settings);
	}

	@Override
	public Predictions setMinWords(int minWords) {
		defaultMinWords = minWords;
		return super.setMinWords(minWords);
	}

	@Override protected boolean isRetryAllowed() { return loadAttempts == 0; }
	@Override public void onAccept(String word, String sequence) {
		lastWord = lastStableWord = "";
		lastStableSequenceLength = 0;
	}

	@Override
	public void load() {
		loadAttempts = 0;
		minWords = defaultMinWords;
		super.load();
	}

	private void loadSimilar() {
		loadAttempts++;
		minWords = defaultMinWords + 1;
		super.load();
	}

	@Override
	protected void onDbWords(ArrayList<String> dbWords, boolean retryAllowed) {
		// @todo: 0125 + 2. Прецаква се, защото директно отиваме на 522, което не съществува, така че нямаме предишна стабилна дума.

		areThereDbWords = !dbWords.isEmpty();

		if (loadAttempts == 0) {
			words.clear();
		} else {
			onWordsChanged.run();
			return;
		}

		if (digitSequence.length() < lastStableSequenceLength) {
			lastStableWord = "";
			lastStableSequenceLength = 0;
		}

		if (areThereDbWords) {
			lastWord = dbWords.get(0);
			words.addAll(dbWords);
		} else {
			if (lastStableWord.isEmpty() && !lastWord.isEmpty()) {
				lastStableWord = lastWord;
				lastStableSequenceLength = digitSequence.length();
			}
			lastWord = "";
			words.addAll(generateWordVariations(lastStableWord));
		}

		if (retryAllowed && !areThereDbWords) {
			loadSimilar();
			return;
		}

		onWordsChanged.run();
	}


	@Override
	protected ArrayList<String> generateWordVariations(String baseWord) {
		baseWord = baseWord == null ? "" : baseWord;
		ArrayList<String> variants = new ArrayList<>();

		try {
			int charIndex = Cheonjiin.getRepeatingDigitsAtEnd(digitSequence) - 1;
			int key = digitSequence.charAt(digitSequence.length() - 1) - '0';
			String variant = baseWord + language.getKeyCharacters(key).get(charIndex);
			variants.add(variant);
		} catch (Exception ignored) {
			variants.add(baseWord);
		}

		return variants;
	}
}
