package io.github.sspanak.tt9.ime.modes;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.Punctuation;
import io.github.sspanak.tt9.preferences.T9Preferences;

public class ModePredictive extends InputMode {
	public int getId() { return MODE_PREDICTIVE; }

	private boolean isEmoji = false;
	private String digitSequence = "";

	// stem filter
	private String stemFilter = "";
	private final int STEM_FILTER_MIN_LENGTH = 2;

	// async suggestion handling
	private Language currentLanguage = null;
	private String lastInputFieldWord = "";
	private static Handler handleSuggestionsExternal;


	ModePredictive() {
		allowedTextCases.add(CASE_UPPER);
		allowedTextCases.add(CASE_CAPITALIZE);
		allowedTextCases.add(CASE_LOWER);
	}


	public boolean onBackspace() {
		if (stemFilter.length() < STEM_FILTER_MIN_LENGTH) {
			stemFilter = "";
		}

		if (digitSequence.length() < 1) {
			stemFilter = "";
			return false;
		}

		digitSequence = digitSequence.substring(0, digitSequence.length() - 1);
		if (stemFilter.length() > digitSequence.length()) {
			stemFilter = stemFilter.substring(0, digitSequence.length() - 1);
		}

		return true;
	}


	public boolean onNumber(Language l, int key, boolean hold, boolean repeat) {
		isEmoji = false;

		if (hold) {
			// hold to type any digit
			reset();
			word = String.valueOf(key);
		}	else if (key == 0) {
			// "0" is " "
			reset();
			word = " ";
		} else if (key == 1 && repeat) {
			// emoticons
			reset();
			isEmoji = true;
			suggestions = Punctuation.Emoji;
		}
		else {
			// words
			super.reset();
			digitSequence += key;
		}

		return true;
	}


	public void reset() {
		super.reset();
		digitSequence = "";
		stemFilter = "";
	}


	final public boolean isPredictive() {
		return true;
	}

	public int getSequenceLength() { return isEmoji ? 2 : digitSequence.length(); }

	public boolean shouldTrackUpDown() { return true; }
	public boolean shouldTrackLeftRight() { return true; }


	/**
	 * shouldAcceptCurrentSuggestion
	 * In this mode, In addition to confirming the suggestion in the input field,
	 * we also increase its' priority. This function determines whether we want to do all this or not.
	 */
	public boolean shouldAcceptCurrentSuggestion(int key, boolean hold, boolean repeat) {
		return
			hold
			// Quickly accept suggestions using "space" instead of pressing "ok" then "space"
			|| key == 0
			// Punctuation is considered "a word", so that we can increase the priority as needed
			// Also, it must break the current word.
			|| (key == 1 && digitSequence.length() > 0 && !digitSequence.endsWith("1"))
			// On the other hand, letters also "break" punctuation.
			|| (key != 1 && digitSequence.endsWith("1"));
	}


	/**
	 * isStemFilterOn
	 * Returns "true" if a filter was applied using "setStem()".
	 */
	public boolean isStemFilterOn() {
		return stemFilter.length() > 0;
	}


	/**
	 * clearStemFilter
	 * Do not filter the suggestions by the word set using "setStem()", use only the digit sequence.
	 */
	public void clearStemFilter() {
		stemFilter = "";
	}


	/**
	 * setStemFilter
	 * Filter the possible suggestions by the given stem. The stem must have
	 * a minimum length of STEM_FILTER_MIN_LENGTH.
	 *
	 * Note that you need to manually get the suggestions again to obtain a filtered list.
	 */
	public boolean setStemFilter(Language language, String stem) {
		if (language == null || stem == null || stem.length() < STEM_FILTER_MIN_LENGTH) {
			return false;
		}

		try {
			digitSequence = language.getDigitSequenceForWord(stem);
			stemFilter = stem;
			return true;
		} catch (Exception e) {
			Logger.w("tt9/setStemFilter", "Ignoring invalid stem filter: " + stem + ". " + e.getMessage());
			return false;
		}
	}


	/**
	 * getSuggestionsAsync
	 * Queries the dictionary database for a list of suggestions matching the current language and
	 * sequence. Returns "false" when there is nothing to do.
	 *
	 * "lastWord" is used for generating suggestions when there are no results.
	 * See: generateSuggestionWhenNone()
	 */
	public boolean getSuggestionsAsync(Handler handler, Language language, String lastWord) {
		if (isEmoji) {
			super.sendSuggestions(handler, suggestions);
			return true;
		}

		if (digitSequence.length() == 0) {
			return false;
		}

		handleSuggestionsExternal = handler;
		lastInputFieldWord = lastWord;
		currentLanguage = language;
		super.reset();

		DictionaryDb.getSuggestions(
			handleSuggestions,
			language,
			digitSequence,
			stemFilter,
			T9Preferences.getInstance().getSuggestionsMin(),
			T9Preferences.getInstance().getSuggestionsMax()
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
			ArrayList<String> suggestions = msg.getData().getStringArrayList("suggestions");
			suggestions = generateSuggestionWhenNone(suggestions, currentLanguage, lastInputFieldWord);

			ModePredictive.super.sendSuggestions(handleSuggestionsExternal, suggestions);
		}
	};

	/**
	 * generateSuggestionWhenNone
	 * When there are no matching suggestions after the last key press, generate a list of possible
	 * ones, so that the user can complete the missing word.
	 */
	private ArrayList<String> generateSuggestionWhenNone(ArrayList<String> suggestions, Language language, String word) {
		if (
			(word == null || word.length() == 0) ||
			(suggestions != null && suggestions.size() > 0) ||
			digitSequence.length() == 0 ||
			digitSequence.charAt(0) == '1'
		) {
			return suggestions;
		}

		// append all letters for the last key
		word = word.substring(0, Math.min(digitSequence.length() - 1, word.length()));
		ArrayList<String> generatedSuggestions = new ArrayList<>();
		int lastSequenceDigit = digitSequence.charAt(digitSequence.length() - 1) - '0';

		for (String keyLetter : language.getKeyCharacters(lastSequenceDigit)) {
			if (keyLetter.charAt(0) - '0' > '9') { // append only letters, not numbers
				generatedSuggestions.add(word + keyLetter);
			}
		}

		// if there are no letters for this key, just append the number
		if (generatedSuggestions.size() == 0) {
			generatedSuggestions.add(word +  digitSequence.charAt(digitSequence.length() - 1));
		}

		return generatedSuggestions;
	}


	/**
	 * onAcceptSuggestion
	 * Bring this word up in the suggestions list next time.
	 */
	public void onAcceptSuggestion(Language language, String currentWord) {
		reset();

		if (currentWord.length() == 0) {
			Logger.i("acceptCurrentSuggestion", "Current word is empty. Nothing to accept.");
			return;
		}

		try {
			String sequence = language.getDigitSequenceForWord(currentWord);
			DictionaryDb.incrementWordFrequency(language, currentWord, sequence);
		} catch (Exception e) {
			Logger.e("tt9/ModePredictive", "Failed incrementing priority of word: '" + currentWord + "'. " + e.getMessage());
		}
	}

	/**
	 * getNextWordTextCase
	 * Dynamically determine text case of words as the user types to reduce key presses.
	 * For example, this function will return CASE_LOWER by default, but CASE_UPPER at the beginning
	 * of a sentence.
	 */
	public int getNextWordTextCase(int currentTextCase, boolean isThereText, String textBeforeCursor) {
		// If the user wants to type in uppercase, this must be for a reason, so we better not override it.
		if (currentTextCase == CASE_UPPER) {
			return -1;
		}

		// start of text
		if (!isThereText) {
			return CASE_CAPITALIZE;
		}

		// start of sentence, excluding after "..."
		Matcher endOfSentenceMatch = Pattern.compile("(?<!\\.)[.?!]\\s*$").matcher(textBeforeCursor);
		if (endOfSentenceMatch.find()) {
			return CASE_CAPITALIZE;
		}

		return CASE_LOWER;
	}
}
