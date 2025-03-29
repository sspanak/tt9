package io.github.sspanak.tt9.ime.modes.predictions;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.TextTools;

public class IdeogramPredictions extends WordPredictions {
	private boolean isTranscriptionFilterAllowed = false;
	private String lastTypedWord = "";
	@NonNull protected ArrayList<String> transcriptions = new ArrayList<>();


	public IdeogramPredictions(SettingsStore settings, TextField textField) {
		super(settings, textField);
		minWords = 1;
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


	@Override
	@NonNull
	protected String getPenultimateWord(@NonNull String currentWord) {
		int currentWordLength = currentWord.length();
		int lastWordLength = lastTypedWord.length();
		int requiredTextLength = currentWordLength + lastWordLength;
		String text = textField.getStringBeforeCursor(requiredTextLength);
//		Logger.d("LOG_TAG", "====+> previous string: " + text);

		return lastWordLength < text.length() ? text.substring(0, lastWordLength) : "";
	}


	/**
	 * Tries to do a best guess what is the previous word and pairs it with the incoming one. Guessing
	 * is because East Asian languages do not have spaces between words, so we try to match the
	 * last typed or just give up.
	 */
	@Override
	protected void pairWithPreviousWord(@NonNull String word, @NonNull String sequence) {
		if (language.hasSpaceBetweenWords()) {
			super.pairWithPreviousWord(word, sequence);
			return;
		}

		if (!settings.getPredictWordPairs() || sequence.length() != digitSequence.length()) {
//			Logger.d("LOG_TAG", "====+> sequence length mismatch: " + sequence.length() + " != " + digitSequence.length());
			return;
		}

		int latinEnd = TextTools.lastIndexOfLatin(word);
		String nativeWord = latinEnd < 0 || latinEnd >= word.length() ? word : word.substring(latinEnd + 1);

		if (lastTypedWord.isEmpty() || (!words.isEmpty() && nativeWord.equals(words.get(0)))) {
			lastTypedWord = nativeWord;
//			Logger.d("LOG_TAG", "====+> Will not pair the first word. native word: " + nativeWord + " first suggestion: " + words.get(0));
//			if (lastTypedWord.isEmpty()) {
//				Logger.d("LOG_TAG", "====+> No previous word to pair with: " + lastTypedWord);
//			}

			return;
		}

		String previousWord = getPenultimateWord(nativeWord);
		if (previousWord.equals(lastTypedWord)) {
//			Logger.d("LOG_TAG", "====+> Pairing words: " + previousWord + " + " + nativeWord);
			DataStore.addWordPair(language, previousWord, nativeWord, sequence);
//		} else {
//			Logger.d("LOG_TAG", "===> Last word mismatch: " + previousWord + " != " + lastTypedWord + ". Not pairing.");
		}

		lastTypedWord = nativeWord;
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
			int trueLength = isTranscriptionFilterAllowed ? TextTools.lastIndexOfLatin(word) : word.length();

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


	/**
	 * Removes the native words and keeps only the unique transcriptions. Directly modifies the words
	 * list, but the original is preserved in this.transcriptions.
	 * Example operation: [SHIWU食物, SHIWU事物, SHIWU事务, SHIZU十足] -> [SHIWU, SHIZU]
	 */
	public void stripNativeWords() {
		HashSet<String> uniqueTranscriptions = new HashSet<>();

		for (int i = 0; i < transcriptions.size(); i++) {
			String transcription = transcriptions.get(i);
			int firstNative = TextTools.lastIndexOfLatin(transcription) + 1;
			uniqueTranscriptions.add(
				firstNative < 1 || firstNative >= transcription.length() ? transcription : transcription.substring(0, firstNative)
			);
		}

		words.clear();
		words.addAll(uniqueTranscriptions);
		Collections.sort(words);
	}


	/**
	 * Removes the Latin transcriptions from native words. Directly modifies the words list, but the
	 * original is preserved in this.transcriptions.
	 * Example operation: [SHIWU食物, SHIZU十足] -> [食物, 十足]
	 */
	public void stripTranscriptions() {
		words.clear();
		for (int i = 0; i < transcriptions.size(); i++) {
			String transcription = transcriptions.get(i);
			int firstNative = TextTools.lastIndexOfLatin(transcription) + 1;
			words.add(firstNative >= transcription.length() ? transcription : transcription.substring(firstNative));
		}
	}


	/**
	 * Similar to "stripNativeWords()", but finds and returns the transcription of the given word.
	 * Returns an empty string if the word is not in the current suggestion list.
	 */
	@NonNull
	public String getTranscription(@NonNull String word) {
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
}
