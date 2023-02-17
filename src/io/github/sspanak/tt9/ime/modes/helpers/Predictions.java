package io.github.sspanak.tt9.ime.modes.helpers;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.db.DictionaryDb;
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
	private Handler wordsChangedHandler;

	// data
	private final ArrayList<String> words = new ArrayList<>();

	// punctuation/emoji
	private final Pattern containsOnly1Regex = Pattern.compile("^1+$");
	private final String maxEmojiSequence;


	public Predictions(SettingsStore settingsStore) {
		emptyDbWarning = new EmptyDatabaseWarning(settingsStore);
		settings = settingsStore;

		// digitSequence limiter when selecting emoji
		// "11" = Emoji level 0, "111" = Emoji level 1,... up to the maximum amount of 1s
		StringBuilder maxEmojiSequenceBuilder = new StringBuilder();
		for (int i = 0; i <= Characters.getEmojiLevels(); i++) {
			maxEmojiSequenceBuilder.append("1");
		}
		maxEmojiSequence = maxEmojiSequenceBuilder.toString();
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

	public Predictions setWordsChangedHandler(Handler handler) {
		wordsChangedHandler = handler;
		return this;
	}

	public ArrayList<String> getList() {
		return words;
	}


	/**
	 * suggestStem
	 * Add the current stem filter to the predictions list, when it has length of X and
	 * the user has pressed X keys (otherwise, it makes no sense to add it).
	 */
	private void suggestStem() {
		if (stem.length() > 0 && stem.length() == digitSequence.length()) {
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
	 * onWordsChanged
	 * Notify the external handler the word list has changed, so they can get the new ones using getList().
	 */
	private void onWordsChanged() {
		wordsChangedHandler.sendEmptyMessage(0);
	}



	/**
	 * load
	 * Queries the dictionary database for a list of words matching the current language and
	 * sequence or loads the static ones.
	 *
	 * Returns "false" on invalid digitSequence.
	 */
	public boolean load() {
		if (digitSequence == null || digitSequence.length() == 0) {
			words.clear();
			onWordsChanged();
			return false;
		}

		if (loadStatic()) {
			onWordsChanged();
		} else {
			DictionaryDb.getSuggestions(
				dbWordsHandler,
				language,
				digitSequence,
				stem,
				settings.getSuggestionsMin(),
				settings.getSuggestionsMax()
			);
		}

		return true;
	}


	/**
	 * loadStatic
	 * Similar to "load()", but loads words that are not in the database.
	 * Returns "false", when there are no static options for the current digitSequence.
	 */
	private boolean loadStatic() {
		// whitespace/special/math characters
		if (digitSequence.equals("0")) {
			words.clear();
			stem = "";
			words.addAll(language.getKeyCharacters(0, false));
		}
		// "00" is a shortcut for the preferred character
		else if (digitSequence.equals("00")) {
			words.clear();
			stem = "";
			words.add(settings.getDoubleZeroChar());
		}
		// emoji
		else if (containsOnly1Regex.matcher(digitSequence).matches()) {
			words.clear();
			stem = "";
			if (digitSequence.length() == 1) {
				words.addAll(language.getKeyCharacters(1, false));
			} else {
				digitSequence = digitSequence.length() <= maxEmojiSequence.length() ? digitSequence : maxEmojiSequence;
				words.addAll(Characters.getEmoji(digitSequence.length() - 2));
			}
		} else {
			return false;
		}

		return true;
	}


	/**
	 * dbWordsHandler
	 * Extracts the words from the Message object, generates extra words, if necessary, then
	 * notifies the external handler it is now possible to use "getList()".
	 * If there were no matches in the database, they will be generated based on the "inputWord".
	 */
	private final Handler dbWordsHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			ArrayList<String> dbWords = msg.getData().getStringArrayList("suggestions");
			dbWords = dbWords != null ? dbWords : new ArrayList<>();

			if (dbWords.size() == 0 && digitSequence.length() > 0) {
				emptyDbWarning.emitOnce(language);
				dbWords = generatePossibleCompletions(inputWord);
			}

			words.clear();
			suggestStem();
			suggestMissingWords(generatePossibleStemVariations(dbWords));
			suggestMissingWords(dbWords);

			onWordsChanged();
		}
	};


	/**
	 * generatePossibleCompletions
	 * When there are no matching suggestions after the last key press, generate a list of possible
	 * ones, so that the user can complete a missing word that is completely different from the ones
	 * in the dictionary.
	 *
	 * For example, if the word is "missin_" and the last pressed key is "4", the results would be:
	 * | missing | missinh | missini |
	 */
	private ArrayList<String> generatePossibleCompletions(String baseWord) {
		ArrayList<String> generatedWords = new ArrayList<>();

		// Make sure the displayed word and the digit sequence, we will be generating suggestions from,
		// have the same length, to prevent visual discrepancies.
		baseWord = (baseWord != null && baseWord.length() > 0) ? baseWord.substring(0, Math.min(digitSequence.length() - 1, baseWord.length())) : "";

		// append all letters for the last digit in the sequence (the last pressed key)
		int lastSequenceDigit = digitSequence.charAt(digitSequence.length() - 1) - '0';
		for (String keyLetter : language.getKeyCharacters(lastSequenceDigit)) {
			// let's skip numbers, because it's weird, for example:
			// | weird | weire | weirf | weir2 |
			if (keyLetter.charAt(0) < '0' || keyLetter.charAt(0) > '9') {
				generatedWords.add(baseWord + keyLetter);
			}
		}

		// if there are no letters for this key, just append the number
		if (generatedWords.size() == 0) {
			generatedWords.add(baseWord + digitSequence.charAt(digitSequence.length() - 1));
		}

		return generatedWords;
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
		if (stem.length() == 0) {
			return variations;
		}

		if (isStemFuzzy && stem.length() == digitSequence.length() - 1) {
			ArrayList<String> allPossibleVariations = generatePossibleCompletions(stem);

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
