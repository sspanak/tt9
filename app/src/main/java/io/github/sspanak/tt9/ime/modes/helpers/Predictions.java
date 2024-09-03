package io.github.sspanak.tt9.ime.modes.helpers;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.EmojiLanguage;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Characters;

public class Predictions {
	private final SettingsStore settings;
	private final TextField textField;

	private String digitSequence;
	private String inputWord;
	private boolean isStemFuzzy;
	private Language language;
	private String stem;

	// async operations
	private Runnable onWordsChanged = () -> {};

	// data
	private boolean areThereDbWords = false;
	private boolean containsGeneratedWords = false;
	private ArrayList<String> words = new ArrayList<>();

	public Predictions(SettingsStore settings, TextField textField) {
		this.settings = settings;
		this.textField = textField;
	}


	public Predictions setLanguage(Language language) {
		this.language = language;
		return this;
	}

	public Predictions setDigitSequence(String digitSequence) {
		this.digitSequence = digitSequence;
		return this;
	}

	public Predictions setIsStemFuzzy(boolean yes) {
		this.isStemFuzzy = yes;
		return this;
	}

	public Predictions setStem(String stem) {
		this.stem = stem;
		return this;
	}

	public Predictions setInputWord(String inputWord) {
		this.inputWord = inputWord.toLowerCase(language.getLocale());
		return this;
	}

	public Predictions setWordsChangedHandler(Runnable handler) {
		onWordsChanged = handler;
		return this;
	}

	public boolean containsGeneratedWords() {
		return containsGeneratedWords;
	}

	public ArrayList<String> getList() {
		return words;
	}

	public boolean noDbWords() {
		return !areThereDbWords;
	}


	/**
	 * load
	 * Queries the dictionary database for a list of words matching the current language and
	 * sequence or loads the static ones.
	 */
	public void load() {
		containsGeneratedWords = false;

		if (digitSequence == null || digitSequence.isEmpty()) {
			words.clear();
			onWordsChanged.run();
			return;
		}

		boolean retryAllowed = !digitSequence.equals(EmojiLanguage.CUSTOM_EMOJI_SEQUENCE);

		DataStore.getWords(
			(dbWords) -> onDbWords(dbWords, retryAllowed),
			language,
			digitSequence,
			stem,
			SettingsStore.SUGGESTIONS_MIN,
			SettingsStore.SUGGESTIONS_MAX
		);

	}

	private void loadWithoutLeadingPunctuation() {
		DataStore.getWords(
			(dbWords) -> {
				char firstChar = inputWord.charAt(0);
				for (int i = 0; i < dbWords.size(); i++) {
					dbWords.set(i, firstChar + dbWords.get(i));
				}
				onDbWords(dbWords, false);
			},
			language,
			digitSequence.substring(1),
			stem.length() > 1 ? stem.substring(1) : "",
			SettingsStore.SUGGESTIONS_MIN,
			SettingsStore.SUGGESTIONS_MAX
		);
	}


	/**
	 * dbWordsHandler
	 * Callback for when the database has finished loading words. If there were no matches in the database,
	 * they will be generated based on the "inputWord". After the word list is compiled, it notifies the
	 * external handler it is now possible to use it with "getList()".
	 */
	private void onDbWords(ArrayList<String> dbWords, boolean isRetryAllowed) {
		// only the first round matters, the second one is just for getting the letters for a given key
		areThereDbWords = !dbWords.isEmpty() && isRetryAllowed;

		// If there were no database words for ",a", try getting the letters only (e.g. "a", "b", "c").
		// We do this to display them in the correct order.
		if (dbWords.isEmpty() && isRetryAllowed && digitSequence.length() == 2 && digitSequence.charAt(0) == '1') {
			loadWithoutLeadingPunctuation();
			return;
		}

		words.clear();
		if (digitSequence.equals(EmojiLanguage.CUSTOM_EMOJI_SEQUENCE)) {
			words.addAll(dbWords);
		} else {
			suggestStem();
			dbWords = rearrangeByPairFrequency(dbWords);
			suggestMissingWords(generatePossibleStemVariations(dbWords));
			suggestMissingWords(dbWords.isEmpty() ? generateWordVariations(inputWord) : dbWords);
			words = insertPunctuationCompletions(words);
		}

		onWordsChanged.run();
	}


	/**
	 * suggestStem
	 * Add the current stem filter to the predictions list, when it has length of X and
	 * the user has pressed X keys (otherwise, it makes no sense to add it).
	 */
	private void suggestStem() {
		if (!stem.isEmpty() && stem.length() == digitSequence.length()) {
			words.add(stem);
		}
	}


	/**
	 * suggestMissingWords
	 * Takes a list of words and appends them to the words list, if they are missing.
	 */
	private void suggestMissingWords(ArrayList<String> newWords) {
		for (String newWord : newWords) {
			if (!words.contains(newWord) && !words.contains(newWord.toLowerCase(language.getLocale()))) {
				words.add(newWord);
			}
		}
	}


	/**
	 * generateWordVariations
	 * When there are no matching suggestions after the last key press, generate a list of possible
	 * ones, so that the user can complete a missing word that is completely different from the ones
	 * in the dictionary.
	 *
	 * For example, if the word is "missin_" and the last pressed key is "4", the results would be:
	 * | missing | missinh | missini |
	 */
	private ArrayList<String> generateWordVariations(String baseWord) {
		ArrayList<String> generatedWords = new ArrayList<>();

		// This function is called from async context, so by the time it is executed, the digit sequence
		// might have been deleted. But in this case, it makes no sense to generate suggestions.
		if (digitSequence.isEmpty()) {
			return generatedWords;
		}

		// Make sure the displayed word and the digit sequence, we will be generating suggestions from,
		// have the same length, to prevent visual discrepancies.
		baseWord = (baseWord != null && !baseWord.isEmpty()) ? baseWord.substring(0, Math.min(digitSequence.length() - 1, baseWord.length())) : "";

		// append all letters for the last digit in the sequence (the last pressed key)
		int lastSequenceDigit = digitSequence.charAt(digitSequence.length() - 1) - '0';
		for (String keyLetter : language.getKeyCharacters(lastSequenceDigit)) {
			if (Character.isAlphabetic(keyLetter.charAt(0)) || Characters.isCombiningPunctuation(language, keyLetter.charAt(0))) {
				generatedWords.add(baseWord + keyLetter);
			}
		}

		// if there are no letters for this key, just append the number
		if (generatedWords.isEmpty()) {
			generatedWords.add(baseWord + digitSequence.charAt(digitSequence.length() - 1));
		}

		containsGeneratedWords = true;
		return generatedWords;
	}


	/**
	 * insertPunctuationCompletions
	 * When given: "you'", for example, this inserts all other 1-key alternatives, like:
	 * "you.", "you?", "you!" and so on. The generated words will be inserted after the direct
	 * database matches and before the fuzzy matches, as if they were direct matches with low frequency.
	 * This is to preserve the sorting by length and frequency.
	 */
	private ArrayList<String> insertPunctuationCompletions(ArrayList<String> dbWords) {
		if (!stem.isEmpty() || dbWords.isEmpty() || digitSequence.length() < 2 || !digitSequence.endsWith("1")) {
			return dbWords;
		}

		ArrayList<String> complementedWords = new ArrayList<>();
		int exactMatchLength = digitSequence.length();

		// shortest database words (exact matches)
		for (String w : dbWords) {
			if (w.length() <= exactMatchLength) {
				complementedWords.add(w);
			}
		}

		// generated "exact matches"
		String baseWord = inputWord.length() == digitSequence.length() - 1 ? inputWord : dbWords.get(0);
		for (String w : generateWordVariations(baseWord)) {
			if (!dbWords.contains(w) && !dbWords.contains(w.toLowerCase(language.getLocale()))) {
				complementedWords.add(w);
			}
		}

		// longer database words (fuzzy matches)
		for (String w : dbWords) {
			if (w.length() > exactMatchLength) {
				complementedWords.add(w);
			}
		}

		containsGeneratedWords = true;
		return complementedWords;
	}


	/**
	 * generatePossibleStemVariations
	 * Similar to generatePossibleCompletions(), but uses the current filter as a base word. This is
	 * used to complement the database results with all possible variations for the next key, when
	 * the stem filter is on.
	 *
	 * It will not generate anything if more than one key was pressed after filtering though.
	 *
	 * For example, if the filter is "extr", the current word is "extr_" and the user has pressed "1",
	 * the database would have returned only "extra", but this function would also
	 * generate: "extrb" and "extrc". This is useful for typing an unknown word, that is similar to
	 * the ones in the dictionary.
	 */
	private ArrayList<String> generatePossibleStemVariations(ArrayList<String> dbWords) {
		ArrayList<String> variations = new ArrayList<>();

		if (isStemFuzzy && !stem.isEmpty() && stem.length() == digitSequence.length() - 1) {
			ArrayList<String> allPossibleVariations = generateWordVariations(stem);

			// first add the known words, because it makes more sense to see them first
			for (String variation : allPossibleVariations) {
				if (dbWords.contains(variation)) {
					variations.add(variation);
				}
			}

			// then add the unknown ones, so they can be used as possible beginnings of new words.
			for (String word : allPossibleVariations) {
				if (!dbWords.contains(word)) {
					variations.add(word);
				}
			}
		}

		containsGeneratedWords = !variations.isEmpty();
		return variations;
	}


	/**
	 * onAccept
	 * This stores common word pairs, so they can be used in "rearrangeByPairFrequency()" method.
	 * For example, if the user types "I am an apple", the word "am" will be suggested after "I",
	 * and "an" after "am", even if "am" frequency was boosted right before typing "an". This both
	 * prevents from suggesting the same word twice in row and makes the suggestions more intuitive
	 * when there are many textonyms for a single sequence.
	 */
	public void onAccept(String newlyAcceptedWord) {
		if (
			!settings.getPredictWordPairs()
			// If the accepted word is longer than the sequence, it is some different word, not a textonym
			// of the fist suggestion. We don't need to store it.
			|| newlyAcceptedWord == null || digitSequence == null
			|| newlyAcceptedWord.length() != digitSequence.length()
			// If the word is the first suggestion, we have already guessed it right, and it makes no
			// sense to store it as a popular pair.
			|| (!words.isEmpty() && words.get(0).equals(newlyAcceptedWord))
		) {
			return;
		}

		DataStore.addPair(language, textField.getWordBeforeCursor(language, 1, true), newlyAcceptedWord);
	}


	/**
	 * rearrangeByPairFrequency
	 * Uses the last two words in the text field to rearrange the suggestions, so that the most popular
	 * one in a pair comes first. This is useful for typing phrases, like "I am an apple". Since, in
	 * "onAccept()", we have remembered the "am" comes after "I" and "an" comes after "am", we will
	 * not suggest the textonyms "am" or "an" twice (depending on which has the highest frequency).
	 */
	private ArrayList<String> rearrangeByPairFrequency(ArrayList<String> words) {
		if (!settings.getPredictWordPairs() || words.size() < 2) {
			return words;
		}

		ArrayList<String> rearrangedWords = new ArrayList<>();
		String penultimateWord = textField.getWordBeforeCursor(language, 1, true);

		int morePopularIndex = -1;
		for (int i = 1; i < words.size(); i++) {
			if (DataStore.containsPair(language, penultimateWord, words.get(i))) {
				rearrangedWords.add(words.get(i));
				morePopularIndex = i;
				break;
			}
		}

		for (int i = 0; i < words.size(); i++) {
			if (i != morePopularIndex) {
				rearrangedWords.add(words.get(i));
			}
		}

		return rearrangedWords;
	}
}
