package io.github.sspanak.tt9.ime.modes;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.Collections;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.Punctuation;
import io.github.sspanak.tt9.preferences.T9Preferences;

public class ModePredictive extends InputMode {
	public int getId() { return MODE_PREDICTIVE; }

	private Language currentLanguage = null;
	private String digitSequence = "";
	private boolean isEmoticon = false;
	private String lastInputFieldWord = "";
	private static Handler handleSuggestionsExternal;



	ModePredictive() {
		allowedTextCases.add(CASE_CAPITALIZE);
		allowedTextCases.add(CASE_LOWER);
		allowedTextCases.add(CASE_UPPER);
	}


	public boolean onBackspace() {
		if (digitSequence.length() < 1) {
			return false;
		}

		digitSequence = digitSequence.substring(0, digitSequence.length() - 1);
		return true;
	}


	public boolean onNumber(Language l, int key, boolean hold, boolean repeat) {
		isEmoticon = false;

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
			isEmoticon = true;
			suggestions = Punctuation.Emoticons;
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
	}


	final public boolean isPredictive() {
		return true;
	}


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
	 * getSuggestionsAsync
	 * Queries the dictionary database for a list of suggestions matching the current language and
	 * sequence. Returns "false" when there is nothing to do.
	 *
	 * "lastWord" is used for generating suggestions when there are no results.
	 * See: generateSuggestionWhenNone()
	 */
	public boolean getSuggestionsAsync(Handler handler, Language language, String lastWord) {
		if (isEmoticon) {
			super.sendSuggestions(handler, suggestions, 2);
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

			ModePredictive.super.sendSuggestions(handleSuggestionsExternal, suggestions, digitSequence.length());
		}
	};

	/**
	 * generateSuggestionWhenNone
	 * When there are no matching suggestions after the last key press, generate a list of possible
	 * ones, so that the user can complete the missing word.
	 */
	private ArrayList<String> generateSuggestionWhenNone(ArrayList<String> suggestions, Language language, String lastWord) {
		if (
			(lastWord == null || lastWord.length() == 0) ||
			(suggestions != null && suggestions.size() > 0) ||
			digitSequence.length() == 0 ||
			digitSequence.charAt(0) == '1'
		) {
			return suggestions;
		}

		lastWord = lastWord.substring(0, Math.min(digitSequence.length() - 1, lastWord.length()));
		try {
			int lastDigit = digitSequence.charAt(digitSequence.length() - 1) - '0';
			lastWord += language.getKeyCharacters(lastDigit).get(0);
		} catch (Exception e) {
			lastWord += digitSequence.charAt(digitSequence.length() - 1);
		}

		return new ArrayList<>(Collections.singletonList(lastWord));
	}


	/**
	 * onAcceptSuggestion
	 * Bring this word up in the suggestions list next time.
	 */
	public void onAcceptSuggestion(Language language, String currentWord) {
		digitSequence = "";

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
}
