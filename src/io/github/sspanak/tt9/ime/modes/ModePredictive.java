package io.github.sspanak.tt9.ime.modes;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.ime.helpers.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.AutoSpace;
import io.github.sspanak.tt9.ime.modes.helpers.AutoTextCase;
import io.github.sspanak.tt9.ime.modes.helpers.Predictions;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class ModePredictive extends InputMode {
	private final SettingsStore settings;

	public int getId() { return MODE_PREDICTIVE; }

	private String digitSequence = "";
	private String lastAcceptedWord = "";
	private String lastAcceptedSequence = "";

	// stem filter
	private boolean isStemFuzzy = false;
	private String stem = "";

	// async suggestion handling
	private static Handler handleSuggestionsExternal;

	// text analysis tools
	private final AutoSpace autoSpace;
	private final AutoTextCase autoTextCase;
	private final Predictions predictions;


	ModePredictive(SettingsStore settings, Language lang) {
		changeLanguage(lang);

		autoSpace = new AutoSpace(settings);
		autoTextCase = new AutoTextCase(settings);
		predictions = new Predictions(settings);

		this.settings = settings;
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
	public boolean onNumber(int number, boolean hold, int repeat) {
		if (hold) {
			// hold to type any digit
			reset();
			autoAcceptTimeout = 0;
			suggestions.add(String.valueOf(number));
		} else {
			// words
			super.reset();
			digitSequence += number;
			if (number == 0 && repeat > 0) {
				autoAcceptTimeout = 0;
			}
		}

		return true;
	}


	@Override
	public void changeLanguage(Language language) {
		super.changeLanguage(language);

		allowedTextCases.clear();
		allowedTextCases.add(CASE_LOWER);
		if (language.hasUpperCase()) {
			allowedTextCases.add(CASE_CAPITALIZE);
			allowedTextCases.add(CASE_UPPER);
		}
	}


	@Override
	public void reset() {
		super.reset();
		digitSequence = "";
		stem = "";
	}


	/**
	 * clearWordStem
	 * Do not filter the suggestions by the word set using "setWordStem()", use only the digit sequence.
	 */
	@Override
	public boolean clearWordStem() {
		if (stem.length() == 0) {
			return false;
		}

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
	public boolean setWordStem(String wordStem, boolean exact) {
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
	 * loadSuggestions
	 * Loads the possible list of suggestions for the current digitSequence.
	 * Returns "false" on invalid sequence.
	 *
	 * "currentWord" is used for generating suggestions when there are no results.
	 * See: Predictions.generatePossibleCompletions()
	 */
	@Override
	public boolean loadSuggestions(Handler handler, String currentWord) {
		predictions
			.setDigitSequence(digitSequence)
			.setIsStemFuzzy(isStemFuzzy)
			.setStem(stem)
			.setLanguage(language)
			.setInputWord(currentWord)
			.setWordsChangedHandler(handleSuggestions);

		handleSuggestionsExternal = handler;

		return predictions.load();
	}


	/**
	 * handleSuggestions
	 * Extracts the suggestions from the Message object and passes them to the actual external Handler.
	 * If there were no matches in the database, they will be generated based on the "lastInputFieldWord".
	 */
	private final Handler handleSuggestions = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message m) {
			suggestions.clear();
			suggestions.addAll(predictions.getList());

			onSuggestionsUpdated(handleSuggestionsExternal);
		}
	};


	/**
	 * onAcceptSuggestion
	 * Bring this word up in the suggestions list next time.
	 */
	@Override
	public void onAcceptSuggestion(String currentWord) {
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
			if (!sequence.startsWith("11") && !sequence.equals("1") && !sequence.startsWith("0")) {
				DictionaryDb.incrementWordFrequency(language, currentWord, sequence);
			}
		} catch (Exception e) {
			Logger.e("tt9/ModePredictive", "Failed incrementing priority of word: '" + currentWord + "'. " + e.getMessage());
		}
	}


	@Override
	protected String adjustSuggestionTextCase(String word, int newTextCase) {
		return autoTextCase.adjustSuggestionTextCase(language, word, newTextCase);
	}

	@Override
	public void determineNextWordTextCase(SettingsStore settings, boolean isThereText, String textBeforeCursor) {
		textCase = autoTextCase.determineNextWordTextCase(isThereText, textCase, textFieldTextCase, textBeforeCursor);
	}

	@Override
	public void nextTextCase() {
		textFieldTextCase = CASE_UNDEFINED; // since it's a user's choice, the default matters no more
		super.nextTextCase();
	}


	/**
	 * shouldAcceptCurrentSuggestion
	 * In this mode, In addition to confirming the suggestion in the input field,
	 * we also increase its' priority. This function determines whether we want to do all this or not.
	 */
	@Override
	public boolean shouldAcceptCurrentSuggestion(int key, boolean hold, boolean repeat) {
		return
			hold
			// Quickly accept suggestions using "space" instead of pressing "ok" then "space"
			|| (key == 0 && !repeat)
			// Punctuation is considered "a word", so that we can increase the priority as needed
			// Also, it must break the current word.
			|| (!language.isPunctuationPartOfWords() && key == 1 && digitSequence.length() > 0 && !digitSequence.endsWith("1"))
			// On the other hand, letters also "break" punctuation.
			|| (!language.isPunctuationPartOfWords() && key != 1 && digitSequence.endsWith("1"))
			|| (digitSequence.endsWith("0") && key != 0);
	}


	@Override
	public boolean shouldAddAutoSpace(InputType inputType, TextField textField, boolean isWordAcceptedManually, int incomingKey, boolean hold, boolean repeat) {
		return autoSpace
			.setLastWord(lastAcceptedWord)
			.setLastSequence(lastAcceptedSequence)
			.setInputType(inputType)
			.setTextField(textField)
			.shouldAddAutoSpace(isWordAcceptedManually, incomingKey, hold, repeat);

	}


	@Override
	public boolean shouldDeletePrecedingSpace(InputType inputType) {
		return autoSpace
			.setLastWord(lastAcceptedWord)
			.setLastSequence(lastAcceptedSequence)
			.setInputType(inputType)
			.setTextField(null)
			.shouldDeletePrecedingSpace();
	}


	@Override public boolean shouldTrackUpDown() { return true; }
	@Override public boolean shouldTrackLeftRight() { return true; }

	@Override final public boolean isPredictive() { return true; }
	@Override public int getSequenceLength() { return digitSequence.length(); }

	@NonNull
	@Override
	public String toString() {
		if (language == null) {
			return "Predictive";
		}

		String modeString = language.getName();
		if (textCase == CASE_UPPER) {
			return modeString.toUpperCase(language.getLocale());
		} else if (textCase == CASE_LOWER && !settings.getAutoTextCase()) {
			return modeString.toLowerCase(language.getLocale());
		} else {
			return modeString;
		}
	}
}
