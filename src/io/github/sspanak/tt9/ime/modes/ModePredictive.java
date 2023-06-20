package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.TextTools;
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

	// stem filter
	private boolean isStemFuzzy = false;
	private String stem = "";

	// async suggestion handling
	private boolean disablePredictions = false;
	private Runnable onSuggestionsUpdated;

	// text analysis tools
	private final AutoSpace autoSpace;
	private final AutoTextCase autoTextCase;
	private final Predictions predictions;


	ModePredictive(SettingsStore settings, Language lang) {
		changeLanguage(lang);
		defaultTextCase();

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
			disablePredictions = true;
			suggestions.add(String.valueOf(number));
		} else {
			// words
			super.reset();
			disablePredictions = false;
			digitSequence += number;
			if (number == 0 && repeat > 0) {
				autoAcceptTimeout = 0;
			}
		}

		return true;
	}


	@Override
	public boolean onOtherKey(int key) {
		reset();

		if (key > 0) {
			disablePredictions = true;
			keyCode = key;
			return true;
		}

		return false;
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
		disablePredictions = false;
		stem = "";
	}


	/**
	 * clearLastAcceptedWord
	 * Removes the last accepted word from the suggestions list and the "digitSequence"
	 * or stops silently, when there is nothing to do.
	 */
	private void clearLastAcceptedWord() {
		if (
			lastAcceptedWord.isEmpty()
			|| suggestions.isEmpty()
			|| !suggestions.get(0).toLowerCase(language.getLocale()).startsWith(lastAcceptedWord.toLowerCase(language.getLocale()))
		) {
			return;
		}

		int lastAcceptedWordLength = lastAcceptedWord.length();
		digitSequence = digitSequence.length() > lastAcceptedWordLength ? digitSequence.substring(lastAcceptedWordLength) : "";

		ArrayList<String> lastSuggestions = new ArrayList<>(suggestions);
		suggestions.clear();
		for (String s : lastSuggestions) {
			suggestions.add(s.length() >= lastAcceptedWordLength ? s.substring(lastAcceptedWordLength) : "");
		}
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
	public boolean setWordStem(String newStem, boolean exact) {
		String sanitizedStem = TextTools.removeNonLetters(newStem);
		if (language == null || sanitizedStem == null || sanitizedStem.length() < 1) {
			return false;
		}

		try {
			// digitSequence = "the raw input", so that everything the user typed is preserved visually
			// stem = "the sanitized input", because filtering by anything that is not a letter makes no sense
			digitSequence = language.getDigitSequenceForWord(newStem);
			stem = sanitizedStem.toLowerCase(language.getLocale());
			isStemFuzzy = !exact;

			Logger.d("tt9/setWordStem", "Stem is now: " + stem + (isStemFuzzy ? " (fuzzy)" : ""));
			return true;
		} catch (Exception e) {
			isStemFuzzy = false;
			stem = "";

			Logger.w("tt9/setWordStem", "Ignoring invalid stem: " + newStem + ". " + e.getMessage());
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
	public void loadSuggestions(Runnable onLoad, String currentWord) {
		if (disablePredictions) {
			super.loadSuggestions(onLoad, currentWord);
			return;
		}

		onSuggestionsUpdated = onLoad;
		predictions
			.setDigitSequence(digitSequence)
			.setIsStemFuzzy(isStemFuzzy)
			.setStem(stem)
			.setLanguage(language)
			.setInputWord(currentWord)
			.setWordsChangedHandler(this::getPredictions)
			.load();
	}


	/**
	 * getPredictions
	 * Gets the currently available Predictions and sends them over to the external caller.
	 */
	private void getPredictions() {
		digitSequence = predictions.getDigitSequence();
		suggestions.clear();
		suggestions.addAll(predictions.getList());

		onSuggestionsUpdated.run();
	}


	/**
	 * onAcceptSuggestion
	 * Bring this word up in the suggestions list next time and if necessary preserves the suggestion list
	 * with "currentWord" cleaned from them.
	 */
	@Override
	public void onAcceptSuggestion(@NonNull String currentWord, boolean preserveWords) {
		lastAcceptedWord = currentWord;

		if (preserveWords) {
			clearLastAcceptedWord();
		} else {
			reset();
		}
		stem = "";

		if (currentWord.isEmpty()) {
			Logger.i("acceptCurrentSuggestion", "Current word is empty. Nothing to accept.");
			return;
		}

		// increment the frequency of the given word
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
	public void determineNextWordTextCase(boolean isThereText, String textBeforeCursor) {
		textCase = autoTextCase.determineNextWordTextCase(isThereText, textCase, textFieldTextCase, textBeforeCursor);
	}

	@Override
	public void nextTextCase() {
		textFieldTextCase = CASE_UNDEFINED; // since it's a user's choice, the default matters no more
		super.nextTextCase();
	}


	/**
	 * shouldAcceptPreviousSuggestion
	 * In this mode, In addition to confirming the suggestion in the input field,
	 * we also increase its' priority. This function determines whether we want to do all this or not.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey) {
		return
			!digitSequence.isEmpty() && (
				(nextKey == 0 && digitSequence.charAt(digitSequence.length() - 1) != '0')
				|| (nextKey != 0 && digitSequence.charAt(digitSequence.length() - 1) == '0')
			);
	}


		/**
	 * shouldAcceptPreviousSuggestion
	 * Variant for post suggestion load analysis.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion() {
		return
			(autoAcceptTimeout == 0 && !digitSequence.startsWith("0"))
			|| (
				!digitSequence.isEmpty()
				&& !predictions.areThereDbWords()
				&& digitSequence.contains("1")
				&& TextTools.containsOtherThan1(digitSequence)
			);
	}


	@Override
	public boolean shouldAddAutoSpace(InputType inputType, TextField textField, boolean isWordAcceptedManually, int nextKey) {
		return autoSpace
			.setLastWord(lastAcceptedWord)
			.setLastSequence()
			.setInputType(inputType)
			.setTextField(textField)
			.shouldAddAutoSpace(isWordAcceptedManually, nextKey);
	}


	@Override
	public boolean shouldDeletePrecedingSpace(InputType inputType) {
		return autoSpace
			.setLastWord(lastAcceptedWord)
			.setInputType(inputType)
			.setTextField(null)
			.shouldDeletePrecedingSpace();
	}


	@Override public boolean shouldTrackUpDown() { return true; }
	@Override public boolean shouldTrackLeftRight() { return true; }

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
