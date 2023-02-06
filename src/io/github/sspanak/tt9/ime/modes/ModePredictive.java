package io.github.sspanak.tt9.ime.modes;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.ime.EmptyDatabaseWarning;
import io.github.sspanak.tt9.ime.helpers.InputFieldHelper;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.Punctuation;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class ModePredictive extends InputMode {
	private final EmptyDatabaseWarning emptyDbWarning;
	private final SettingsStore settings;

	public int getId() { return MODE_PREDICTIVE; }

	private String digitSequence = "";
	private String lastAcceptedWord = "";
	private String lastAcceptedSequence = "";

	// stem filter
	private boolean isStemFuzzy = false;
	private String stem = "";

	// async suggestion handling
	private Language currentLanguage = null;
	private String currentInputFieldWord = "";
	private static Handler handleSuggestionsExternal;

	// auto text case selection
	private final Pattern startOfSentenceRegex = Pattern.compile("(?<!\\.)[.?!¿¡]\\s*$");

	// punctuation/emoji
	private final Pattern containsOnly1Regex = Pattern.compile("^1+$");
	private final String maxEmojiSequence;


	ModePredictive(SettingsStore settings) {
		allowedTextCases.add(CASE_LOWER);
		allowedTextCases.add(CASE_CAPITALIZE);
		allowedTextCases.add(CASE_UPPER);

		emptyDbWarning = new EmptyDatabaseWarning(settings);
		this.settings = settings;

		// digitSequence limiter when selecting emoji
		// "11" = Emoji level 0, "111" = Emoji level 1,... up to the maximum amount of 1s
		StringBuilder maxEmojiSequenceBuilder = new StringBuilder();
		for (int i = 0; i <= Punctuation.getEmojiLevels(); i++) {
			maxEmojiSequenceBuilder.append("1");
		}
		maxEmojiSequence = maxEmojiSequenceBuilder.toString();
	}


	@Override
	public boolean onBackspace() {
		if (digitSequence.length() < 1) {
			clearWordStem();
			return false;
		}

		digitSequence = digitSequence.substring(0, digitSequence.length() - 1);
		if (digitSequence.length() == 0) {
			clearWordStem();
		} else if (stem.length() > digitSequence.length()) {
			stem = stem.substring(0, digitSequence.length() - 1);
		}

		return true;
	}


	@Override
	public boolean onNumber(Language l, int key, boolean hold, int repeat) {
		if (hold) {
			// hold to type any digit
			reset();
			word = String.valueOf(key);
		} else if (key == 0 && repeat > 0) {
			// repeat "0" is a shortcut for the preferred character (default: space)
			reset();
			word = settings.getDoubleZeroChar();
		} else {
			// words
			super.reset();
			digitSequence += key;
		}

		return true;
	}


	@Override
	public void reset() {
		super.reset();
		digitSequence = "";
		stem = "";
	}


	/**
	 * shouldAddAutoSpace
	 * When the "auto-space" settings is enabled, this determines whether to automatically add a space
	 * at the end of a sentence or after accepting a suggestion. This allows faster typing, without
	 * pressing space.
	 *
	 * See the helper functions for the list of rules.
	 */
	@Override
	public boolean shouldAddAutoSpace(InputConnection inputConnection, EditorInfo inputField, boolean isWordAcceptedManually, int incomingKey, boolean hold) {
		return
			settings.getAutoSpace()
			&& !hold
			&& (
				shouldAddAutoSpaceAfterPunctuation(inputField, incomingKey)
				|| shouldAddAutoSpaceAfterWord(inputField, isWordAcceptedManually)
			)
			&& !InputFieldHelper.isThereSpaceAhead(inputConnection);
	}


	/**
	 * shouldDeletePrecedingSpace
	 * When the "auto-space" settings is enabled, determine whether to delete spaces before punctuation.
	 * This allows automatic conversion from: "words ." to: "words."
	 */
	@Override
	public boolean shouldDeletePrecedingSpace(EditorInfo inputField) {
		return
			settings.getAutoSpace()
			&& (
				lastAcceptedWord.equals(".")
				|| lastAcceptedWord.equals(",")
				|| lastAcceptedWord.equals(";")
				|| lastAcceptedWord.equals(":")
				|| lastAcceptedWord.equals("!")
				|| lastAcceptedWord.equals("?")
				|| lastAcceptedWord.equals(")")
				|| lastAcceptedWord.equals("]")
				|| lastAcceptedWord.equals("'")
				|| lastAcceptedWord.equals("@")
			)
			&& !InputFieldHelper.isSpecializedTextField(inputField);
	}


	/**
	 * shouldAddAutoSpaceAfterPunctuation
	 * Determines whether to automatically adding a space after certain punctuation signs makes sense.
	 * The rules are similar to the ones in the standard Android keyboard (with some exceptions,
	 * because we are not using a QWERTY keyboard here).
	 */
	private boolean shouldAddAutoSpaceAfterPunctuation(EditorInfo inputField, int incomingKey) {
		return
			incomingKey != 0
			&& (
				lastAcceptedWord.endsWith(".")
				|| lastAcceptedWord.endsWith(",")
				|| lastAcceptedWord.endsWith(";")
				|| lastAcceptedWord.endsWith(":")
				|| lastAcceptedWord.endsWith("!")
				|| lastAcceptedWord.endsWith("?")
				|| lastAcceptedWord.endsWith(")")
				|| lastAcceptedWord.endsWith("]")
				|| lastAcceptedWord.endsWith("%")
			)
			&& !InputFieldHelper.isSpecializedTextField(inputField);
	}


	/**
	 * shouldAddAutoSpaceAfterPunctuation
	 * Similar to "shouldAddAutoSpaceAfterPunctuation()", but determines whether to add a space after
	 * words.
	 */
	private boolean shouldAddAutoSpaceAfterWord(EditorInfo inputField, boolean isWordAcceptedManually) {
		return
			// Do not add space when auto-accepting words, because it feels very confusing when typing.
			isWordAcceptedManually
			// Secondary punctuation
			&& !lastAcceptedSequence.equals("0")
			// Emoji
			&& !lastAcceptedSequence.startsWith("1")
			&& !InputFieldHelper.isSpecializedTextField(inputField);
	}


	/**
	 * shouldAcceptCurrentSuggestion
	 * In this mode, In addition to confirming the suggestion in the input field,
	 * we also increase its' priority. This function determines whether we want to do all this or not.
	 */
	@Override
	public boolean shouldAcceptCurrentSuggestion(Language language, int key, boolean hold, boolean repeat) {
		return
			hold
			// Quickly accept suggestions using "space" instead of pressing "ok" then "space"
			|| key == 0
			// Punctuation is considered "a word", so that we can increase the priority as needed
			// Also, it must break the current word.
			|| (!language.isPunctuationPartOfWords() && key == 1 && digitSequence.length() > 0 && !digitSequence.endsWith("1"))
			// On the other hand, letters also "break" punctuation.
			|| (!language.isPunctuationPartOfWords() && key != 1 && digitSequence.endsWith("1"))
			|| (digitSequence.endsWith("0"));
	}


	/**
	 * clearWordStem
	 * Do not filter the suggestions by the word set using "setWordStem()", use only the digit sequence.
	 */
	@Override
	public boolean clearWordStem() {
		stem = "";
		Logger.d("tt9/setWordStem", "Stem filter cleared");

		return true;
	}


	/**
	 * setWordStem
	 * Filter the possible suggestions by the given stem.
	 *
	 * If exact is "true", the database will be filtered by "stem" and if the stem word is missing,
	 * it will be added to the suggestions list.
	 * For example: "exac_" -> "exac", {database suggestions...}
	 *
	 * If "exact" is false, in addition to the above, all possible next combinations will be
	 * added to the suggestions list, even if they make no sense.
	 * For example: "exac_" -> "exac", "exact", "exacu", "exacv", {database suggestions...}
	 *
	 *
	 * Note that you need to manually get the suggestions again to obtain a filtered list.
	 */
	@Override
	public boolean setWordStem(Language language, String wordStem, boolean exact) {
		if (language == null || wordStem == null || wordStem.length() < 1) {
			return false;
		}

		try {
			digitSequence = language.getDigitSequenceForWord(wordStem);
			isStemFuzzy = !exact;
			stem = digitSequence.startsWith("0") || digitSequence.startsWith("1") ? "" : wordStem.toLowerCase(language.getLocale());

			Logger.d("tt9/setWordStem", "Stem is now: " + stem + (isStemFuzzy ? " (fuzzy)" : ""));
			return true;
		} catch (Exception e) {
			isStemFuzzy = false;
			stem = "";

			Logger.w("tt9/setWordStem", "Ignoring invalid stem: " + wordStem + ". " + e.getMessage());
			return false;
		}
	}

	/**
	 * getWordStem
	 * If "setWordStem()" has accepted a new stem by returning "true", it can be obtained using this.
	 */
	@Override
	public String getWordStem() {
		return stem;
	}


	/**
	 * isStemFilterFuzzy
	 * Returns how strict the stem filter is.
	 */
	@Override
	public boolean isStemFilterFuzzy() {
		return isStemFuzzy;
	}

	/**
	 * loadStaticSuggestions
	 * Similar to "loadSuggestions()", but loads suggestions that are not in the database.
	 * Returns "false", when there are no static suggestions for the current digitSequence.
	 */
	private boolean loadStaticSuggestions(Language language) {
		if (digitSequence.equals("0")) {
			stem = "";
			suggestions = language.getKeyCharacters(0, false);
		} else if (containsOnly1Regex.matcher(digitSequence).matches()) {
			stem = "";
			if (digitSequence.length() == 1) {
				suggestions = language.getKeyCharacters(1, false);
			} else {
				digitSequence = digitSequence.length() <= maxEmojiSequence.length() ? digitSequence : maxEmojiSequence;
				suggestions = Punctuation.getEmoji(digitSequence.length() - 2);
			}
		} else {
			return false;
		}

		return true;
	}


	/**
	 * loadSuggestions
	 * Queries the dictionary database for a list of suggestions matching the current language and
	 * sequence. Returns "false" when there is nothing to do.
	 *
	 * "lastWord" is used for generating suggestions when there are no results.
	 * See: generatePossibleCompletions()
	 */
	@Override
	public boolean loadSuggestions(Handler handler, Language language, String currentWord) {
		if (loadStaticSuggestions(language)) {
			super.onSuggestionsUpdated(handler);
			return true;
		}

		if (digitSequence.length() == 0) {
			suggestions = new ArrayList<>();
			return false;
		}

		handleSuggestionsExternal = handler;
		currentInputFieldWord = currentWord.toLowerCase(language.getLocale());
		currentLanguage = language;
		super.reset();

		DictionaryDb.getSuggestions(
			handleSuggestions,
			language,
			digitSequence,
			stem,
			settings.getSuggestionsMin(),
			settings.getSuggestionsMax()
		);

		return true;
	}


	/**
	 * handleSuggestions
	 * Extracts the suggestions from the Message object and passes them to the actual external Handler.
	 * If there were no matches in the database, they will be generated based on the "lastInputFieldWord".
	 */
	private final Handler handleSuggestions = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			ArrayList<String> dbSuggestions = msg.getData().getStringArrayList("suggestions");
			dbSuggestions = dbSuggestions == null ? new ArrayList<>() : dbSuggestions;

			if (dbSuggestions.size() == 0 && digitSequence.length() > 0) {
				emptyDbWarning.emitOnce(currentLanguage);
				dbSuggestions = generatePossibleCompletions(currentLanguage, currentInputFieldWord);
			}

			suggestions.clear();
			suggestStem();
			suggestions.addAll(generatePossibleStemVariations(currentLanguage, dbSuggestions));
			suggestMoreWords(dbSuggestions);

			ModePredictive.super.onSuggestionsUpdated(handleSuggestionsExternal);
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
	private ArrayList<String> generatePossibleCompletions(Language language, String baseWord) {
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
	private ArrayList<String> generatePossibleStemVariations(Language language, ArrayList<String> dbSuggestions) {
		ArrayList<String> variations = new ArrayList<>();
		if (stem.length() == 0) {
			return variations;
		}

		if (isStemFuzzy && stem.length() == digitSequence.length() - 1) {
			ArrayList<String> allPossibleVariations = generatePossibleCompletions(language, stem);

			// first add the known words, because it makes more sense to see them first
			for (String word : allPossibleVariations) {
				if (dbSuggestions.contains(word)) {
					variations.add(word);
				}
			}

			// then add the unknown ones, so they can be used as possible beginnings of new words.
			for (String word : allPossibleVariations) {
				if (!dbSuggestions.contains(word)) {
					variations.add(word);
				}
			}
		}

		return variations;
	}

	/**
	 * suggestStem
	 * Add the current stem filter to the suggestion list, when it has length of X and
	 * the user has pressed X keys.
	 */
	private void suggestStem() {
		if (stem.length() > 0 && stem.length() == digitSequence.length()) {
			suggestions.add(stem);
		}
	}


	/**
	 * suggestMoreWords
	 * Takes a list of words and appends them to the suggestion list, if they are missing.
	 */
	private void suggestMoreWords(ArrayList<String> newSuggestions) {
		for (String word : newSuggestions) {
			if (!suggestions.contains(word)) {
				suggestions.add(word);
			}
		}
	}


	/**
	 * onAcceptSuggestion
	 * Bring this word up in the suggestions list next time.
	 */
	@Override
	public void onAcceptSuggestion(Language language, String currentWord) {
		lastAcceptedWord = currentWord;
		lastAcceptedSequence = digitSequence;
		reset();

		if (currentWord.length() == 0) {
			Logger.i("acceptCurrentSuggestion", "Current word is empty. Nothing to accept.");
			return;
		}

		try {
			String sequence = language.getDigitSequenceForWord(currentWord);

			// emoji and punctuation are not in the database, so there is no point in
			// running queries that would update nothing
			if (!sequence.startsWith("11") && !sequence.equals("1") && !sequence.equals("0")) {
				DictionaryDb.incrementWordFrequency(language, currentWord, sequence);
			}
		} catch (Exception e) {
			Logger.e("tt9/ModePredictive", "Failed incrementing priority of word: '" + currentWord + "'. " + e.getMessage());
		}
	}


	/**
	 * adjustSuggestionTextCase
	 * In addition to uppercase/lowercase, here we use the result from determineNextWordTextCase(),
	 * to conveniently start sentences with capitals or whatnot.
	 *
	 * Also, by default we preserve any  mixed case words in the dictionary,
	 * for example: "dB", "Mb", proper names, German nouns, that always start with a capital,
	 * or Dutch words such as: "'s-Hertogenbosch".
	 */
	@Override
	protected String adjustSuggestionTextCase(String word, int newTextCase, Language language) {
		switch (newTextCase) {
			case CASE_UPPER:
				return word.toUpperCase(language.getLocale());
			case CASE_LOWER:
				return word.toLowerCase(language.getLocale());
			case CASE_CAPITALIZE:
				return language.isMixedCaseWord(word) ? word : language.capitalize(word);
			case CASE_DICTIONARY:
				return language.isMixedCaseWord(word) ? word : word.toLowerCase(language.getLocale());
			default:
				return word;
		}
	}


	/**
	 * determineNextWordTextCase
	 * Dynamically determine text case of words as the user types, to reduce key presses.
	 * For example, this function will return CASE_LOWER by default, but CASE_UPPER at the beginning
	 * of a sentence.
	 */
	@Override
	public void determineNextWordTextCase(SettingsStore settings, boolean isThereText, String textBeforeCursor) {
		if (!settings.getAutoTextCase()) {
			return;
		}

		// If the user wants to type in uppercase, this must be for a reason, so we better not override it.
		if (textCase == CASE_UPPER) {
			return;
		}

		if (textFieldTextCase != CASE_UNDEFINED) {
			textCase = textFieldTextCase;
			return;
		}

		// start of text
		if (!isThereText) {
			textCase = CASE_CAPITALIZE;
			return;
		}

		// start of sentence, excluding after "..."
		if (startOfSentenceRegex.matcher(textBeforeCursor).find()) {
			textCase = CASE_CAPITALIZE;
			return;
		}

		textCase = CASE_DICTIONARY;
	}


	@Override
	public void nextTextCase() {
		textFieldTextCase = CASE_UNDEFINED; // since it's a user's choice, the default matters no more
		super.nextTextCase();
	}

	@Override final public boolean isPredictive() { return true; }
	@Override public int getSequenceLength() { return digitSequence.length(); }
	@Override public boolean shouldTrackUpDown() { return true; }
	@Override public boolean shouldTrackLeftRight() { return true; }
}
