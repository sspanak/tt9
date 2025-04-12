package io.github.sspanak.tt9.ime.modes.predictions;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.TextTools;

public class IdeogramPredictions extends WordPredictions {
	private boolean isTranscriptionFilterAllowed = false;
	private String lastTypedWord = "";
	@NonNull protected ArrayList<String> transcriptions = new ArrayList<>();
	@NonNull protected ArrayList<String> lastTranscriptions = new ArrayList<>();


	public IdeogramPredictions(SettingsStore settings, TextField textField) {
		super(settings, textField);
		minWords = 1;
		onlyExactMatches = true;

		// Prevent incorrect ordering of words that have the same sequence, but different character lengths.
		// In East Asian languages, we almost always want exact matches, so anything that appears longer, is
		// usually only spelled longer, not that it contains more sounds.
		// For example, "KONO" = "この" and "九". "九" must not come before "この", because it is shorter.
		orderWordsByLength = false;
	}


	@Override
	public Predictions setLanguage(@NonNull Language language) {
		isTranscriptionFilterAllowed = language.hasTranscriptionsEmbedded();
		return super.setLanguage(language);
	}


	@Override
	public void load() {
		if (digitSequence.isEmpty()) {
			transcriptions.clear();
		}
		super.load();
	}


	@Override
	protected void onDbWords(ArrayList<String> dbWords, boolean isRetryAllowed) {
		lastTranscriptions = new ArrayList<>(transcriptions); // backup in case of auto-accept, so that we can still find previous transcriptions
		transcriptions = onlyExactMatches ? reduceFuzzyMatches(dbWords, SettingsStore.SUGGESTIONS_MAX) : dbWords;
		words = new ArrayList<>(transcriptions);
		areThereDbWords = !words.isEmpty();
		if (!areThereDbWords) {
			onNoWords();
		}

		onWordsChanged.run();
	}


	/**
	 * Here we don't need to generate a new digit sequence, because either the word was an exact search
	 * match (the digit sequence is the current one), or it was a fuzzy match (a longer sequence), that
	 * must not cause a frequency change, because it belongs to a different position group.
	 */
	public void onAcceptIdeogram(String word) throws InvalidLanguageCharactersException {
		super.onAccept(getTranscription(word) + word, digitSequence);
	}


	protected void onNoWords() {
		if (digitSequence.length() == 1) {
			transcriptions = generateWordVariations(null);
			words = new ArrayList<>(transcriptions);
		}
	}


	@Override
	@NonNull
	protected String getPenultimateWord(@NonNull String currentWord) {
		final int lastWordLength = lastTypedWord.length();
		if (lastWordLength == 0) {
			return "";
		}

		final int currentWordLength = currentWord.length();
		final int requiredTextLength = currentWordLength + lastWordLength;
		String text = textField.getStringBeforeCursor(requiredTextLength);
		final int textLength = text.length();
		if (textLength == 0) {
			return "";
		}

		if (text.endsWith(currentWord) && textLength > currentWordLength) {
			text = text.substring(0, textLength - currentWordLength);
		}

		return text.contains(lastTypedWord) ? lastTypedWord : "";
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
		if (!areThereDbWords) {
			return;
		}

		HashSet<String> uniqueTranscriptions = new HashSet<>();

		for (int i = 0; i < transcriptions.size(); i++) {
			uniqueTranscriptions.add(stripNativeWord(transcriptions.get((i))));
		}

		words.clear();
		words.addAll(uniqueTranscriptions);
		Collections.sort(words);
	}


	/**
	 * Does the actual stripping of the native word from the transcription for stripNativeWords().
	 */
	protected String stripNativeWord(@NonNull String dbTranscription) {
		int firstNative = TextTools.lastIndexOfLatin(dbTranscription) + 1;
		return firstNative < 1 || firstNative >= dbTranscription.length() ? dbTranscription : dbTranscription.substring(0, firstNative);
	}


	/**
	 * Removes the Latin transcriptions from native words. Directly modifies the words list, but the
	 * original is preserved in this.transcriptions.
	 * Example operation: [SHIWU食物, SHIZU十足] -> [食物, 十足]
	 */
	public void stripTranscriptions() {
		if (!areThereDbWords) {
			return;
		}

		words.clear();
		for (int i = 0; i < transcriptions.size(); i++) {
			String transcription = transcriptions.get(i);
			int firstNative = TextTools.lastIndexOfLatin(transcription) + 1;
			words.add(firstNative >= transcription.length() ? transcription : transcription.substring(firstNative));
		}
	}


	/**
	 * Similar to "stripNativeWords()", but finds and returns the transcription of the given word.
	 * In case of an auto-accept, the `transcriptions` would be empty, so we check the `lastTranscriptions`.
	 * If no transcription is found, an empty string is returned.
	 */
	@NonNull
	public String getTranscription(@NonNull String word) {
		String transcription = getTranscription(word, transcriptions);
		return transcription.isEmpty() ? getTranscription(word, lastTranscriptions) : transcription;
	}


	@NonNull
	private String getTranscription(@NonNull String word, @NonNull ArrayList<String> transcriptionList) {
		for (String w : transcriptionList) {
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
