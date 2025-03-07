package io.github.sspanak.tt9.ime.modes.predictions;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.TextTools;

public class IdeogramPredictions extends WordPredictions {
	@NonNull protected ArrayList<String> transcriptions = new ArrayList<>();
	protected Runnable onWordsPostProcessed = () -> {};
	private boolean isTranscriptionFilterAllowed = false;


	public IdeogramPredictions(SettingsStore settings, TextField textField) {
		super(settings, textField);
		minWords = 1;
		maxWords = -1;
		onlyExactMatches = true;
		onWordsChanged = this::onWordsLoaded;
	}


	@Override
	public Predictions setLanguage(@NonNull Language language) {
		isTranscriptionFilterAllowed = language.hasTranscriptionsEmbedded();
		return super.setLanguage(language);
	}


	@Override
	public void setWordsChangedHandler(Runnable onWordsLoaded) {
		onWordsPostProcessed = onWordsLoaded;
	}


	@Override
	public void load() {
		transcriptions.clear();
		super.load();
	}


	private void onWordsLoaded() {
		if (onlyExactMatches) {
			words = reduceFuzzyMatches(SettingsStore.SUGGESTIONS_MAX);
		}
		transcriptions = new ArrayList<>(words);
		onWordsPostProcessed.run();
	}


	public void onAcceptTranscription(String word, String transcription, String sequence) {
		super.onAccept(transcription + word, sequence);
	}


	/**
	 * Keeps all exact matches and the first n fuzzy matches. Unlike Latin- or Cyrillic-based languages,
	 * ideograms do not "start with" a sequence of characters, so fuzzy matches have little value.
	 * Just keep some of them, in case there are no exact matches.
	 */
	@NonNull
	public ArrayList<String> reduceFuzzyMatches(int maxWords) {
		if (words.isEmpty()) {
			return words;
		}

		ArrayList<String> shortWords = new ArrayList<>();
		final int MAX_LENGTH = Math.max(digitSequence.length() + 1, words.get(0).length());

		for (int i = 0, longWords = 0, end = words.size(); i < end; i++) {
			String word = words.get(i);
			int trueLength = isTranscriptionFilterAllowed ? word.replaceAll(TextTools.NON_LATIN, "").length() : word.length();

			if (trueLength < MAX_LENGTH) {
				shortWords.add(word);
			}

			if (trueLength >= MAX_LENGTH && longWords <= maxWords) {
				longWords++;
				shortWords.add(word);
			}
		}

		return shortWords;
	}


	public void stripTranscriptions() {
		words.clear();
		for (int i = 0; i < transcriptions.size(); i++) {
			String cleanWord = transcriptions.get(i).replaceAll(TextTools.ONLY_LATIN, "");
			words.add(cleanWord.isEmpty() ? transcriptions.get(i) : cleanWord);
		}
	}


	@NonNull
	public String getTranscription(String word) {
		for (String w : transcriptions) {
			if (w.endsWith(word)) {
				return w.replace(word, "");
			}
		}

		return "";
	}
}
