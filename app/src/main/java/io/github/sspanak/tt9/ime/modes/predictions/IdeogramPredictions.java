package io.github.sspanak.tt9.ime.modes.predictions;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class IdeogramPredictions extends WordPredictions {
	@NonNull protected ArrayList<String> transcriptions = new ArrayList<>();
	private boolean isTranscriptionFilterAllowed = false;


	public IdeogramPredictions(SettingsStore settings, TextField textField) {
		super(settings, textField);
		minWords = 1;
		maxWords = -1;
		onlyExactMatches = true;
	}


	@Override
	public Predictions setLanguage(@NonNull Language language) {
		isTranscriptionFilterAllowed = language.hasTranscriptionsEmbedded();
		return super.setLanguage(language);
	}


	@Override
	public void load() {
		transcriptions.clear();
		super.load();
	}


	@Override
	protected void onDbWords(ArrayList<String> dbWords, boolean isRetryAllowed) {
		transcriptions = onlyExactMatches ? reduceFuzzyMatches(dbWords, SettingsStore.SUGGESTIONS_MAX) : dbWords;
		words = new ArrayList<>(transcriptions);
		areThereDbWords = !words.isEmpty();
		onWordsChanged.run();
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
	public ArrayList<String> reduceFuzzyMatches(ArrayList<String> allWords, int maxWords) {
		if (allWords.isEmpty()) {
			return allWords;
		}

		ArrayList<String> shortWords = new ArrayList<>();
		final int MAX_LENGTH = Math.max(digitSequence.length() + 1, allWords.get(0).length());

		for (int i = 0, longWords = 0, end = allWords.size(); i < end; i++) {
			String word = allWords.get(i);
			int trueLength = isTranscriptionFilterAllowed ? indexOfTranscriptionEnd(word) : word.length();

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


	public void stripNativeWords() {
		HashSet<String> uniqueTranscriptions = new HashSet<>();

		for (int i = 0; i < transcriptions.size(); i++) {
			String transcription = transcriptions.get(i);
			int firstNative = indexOfTranscriptionEnd(transcription) + 1;
			uniqueTranscriptions.add(
				firstNative < 1 || firstNative >= transcription.length() ? transcription : transcription.substring(0, firstNative)
			);
		}

		words.clear();
		words.addAll(uniqueTranscriptions);
		Collections.sort(words);
	}


	public void stripTranscriptions() {
		words.clear();
		for (int i = 0; i < transcriptions.size(); i++) {
			String transcription = transcriptions.get(i);
			int firstNative = indexOfTranscriptionEnd(transcription) + 1;
			words.add(firstNative >= transcription.length() ? transcription : transcription.substring(firstNative));
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


	public void orderByPairs() {
		words = rearrangeByPairFrequency(words);
	}


	private int indexOfTranscriptionEnd(@NonNull String word) {
		for (int i = word.length() - 1; i >= 0; i--) {
			char ch = word.charAt(i);
			if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
				return i;
			}
		}

		return -1;
	}
}
