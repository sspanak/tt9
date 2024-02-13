package io.github.sspanak.tt9.ime.modes.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.ime.EmptyDatabaseWarning;
import io.github.sspanak.tt9.languages.Characters;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class Predictions {
	private final EmptyDatabaseWarning emptyDbWarning;
	private final SettingsStore settings;

	private Language language;
	private String digitSequence;
	private boolean isStemFuzzy;
	private String stem;
	private String inputWord;

	// async operations
	private Runnable onWordsChanged = () -> {};

	// data
	private boolean areThereDbWords = false;
	private ArrayList<String> words = new ArrayList<>();

	// static sequences
	public final static String SPECIAL_CHAR_SEQUENCE = "0";
	public final static String PREFERRED_CHAR_SEQUENCE = "00";
	public final static String PUNCTUATION_SEQUENCE = "1";
	private final Pattern EMOJI_SEQUENCE = Pattern.compile("^1{2,}$");


	public Predictions(SettingsStore settingsStore) {
		emptyDbWarning = new EmptyDatabaseWarning();
		settings = settingsStore;
	}


	public Predictions setLanguage(Language language) {
		this.language = language;
		return this;
	}

	public Predictions setDigitSequence(String digitSequence) {
		this.digitSequence = digitSequence;
		return this;
	}

	public String getDigitSequence() {
		return digitSequence;
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

	public ArrayList<String> getList() {
		return words;
	}

	public boolean noDbWords() {
		return !areThereDbWords;
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
	 * load
	 * Queries the dictionary database for a list of words matching the current language and
	 * sequence or loads the static ones.
	 */
	public void load() {
		if (digitSequence == null || digitSequence.isEmpty()) {
			words.clear();
			onWordsChanged.run();
			return;
		}

		if (loadStatic()) {
			onWordsChanged.run();
		} else {
			WordStoreAsync.getWords(
				(words) -> onDbWords(words, true),
				language,
				digitSequence,
				stem,
				SettingsStore.SUGGESTIONS_MIN,
				SettingsStore.SUGGESTIONS_MAX
			);
		}
	}


	/**
	 * loadStatic
	 * Similar to "load()", but loads words that are not in the database.
	 * Returns "false", when there are no static options for the current digitSequence.
	 */
	private boolean loadStatic() {
		ArrayList<String> newWords = null;

		if (digitSequence.equals(SPECIAL_CHAR_SEQUENCE)) {
			newWords = language.getKeyCharacters(0, false);
		}
		else if (digitSequence.equals(PREFERRED_CHAR_SEQUENCE)) {
			newWords = new ArrayList<>(Collections.singletonList(settings.getDoubleZeroChar()));
		}
		else if (digitSequence.equals(PUNCTUATION_SEQUENCE)) {
			newWords = language.getKeyCharacters(1, false);
		}
		else if (EMOJI_SEQUENCE.matcher(digitSequence).matches()) {
			if (digitSequence.length() > Characters.getEmojiLevels()) {
				digitSequence = digitSequence.substring(0, Characters.getEmojiLevels() + 1);
			}
			newWords = Characters.getEmoji(digitSequence.length() - 2);
		}

		if (newWords == null) {
			return false;
		}

		stem = "";
		words.clear();
		words.addAll(newWords);

		return true;
	}

	private void loadWithoutLeadingPunctuation() {
		WordStoreAsync.getWords(
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

		if (dbWords.isEmpty() && !digitSequence.isEmpty()) {
			emptyDbWarning.emitOnce(language);
		}

		words.clear();
		suggestStem();
		suggestMissingWords(generatePossibleStemVariations(dbWords));
		suggestMissingWords(dbWords.isEmpty() ? generateWordVariations(inputWord) : dbWords);
		words = insertPunctuationCompletions(words);

		onWordsChanged.run();
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

		// Make sure the displayed word and the digit sequence, we will be generating suggestions from,
		// have the same length, to prevent visual discrepancies.
		baseWord = (baseWord != null && !baseWord.isEmpty()) ? baseWord.substring(0, Math.min(digitSequence.length() - 1, baseWord.length())) : "";

		// append all letters for the last digit in the sequence (the last pressed key)
		int lastSequenceDigit = digitSequence.charAt(digitSequence.length() - 1) - '0';
		for (String keyLetter : language.getKeyCharacters(lastSequenceDigit, false)) {
			generatedWords.add(baseWord + keyLetter);
		}

		// if there are no letters for this key, just append the number
		if (generatedWords.isEmpty()) {
			generatedWords.add(baseWord + digitSequence.charAt(digitSequence.length() - 1));
		}

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
		for (String w : generateWordVariations(dbWords.get(0))) {
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

		return variations;
	}
}
