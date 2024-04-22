package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.ime.helpers.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.AutoSpace;
import io.github.sspanak.tt9.ime.modes.helpers.AutoTextCase;
import io.github.sspanak.tt9.ime.modes.helpers.Predictions;
import io.github.sspanak.tt9.languages.EmojiLanguage;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;

public class ModePredictive extends InputMode {
	private final String LOG_TAG = getClass().getSimpleName();

	private final SettingsStore settings;

	public int getId() { return MODE_PREDICTIVE; }

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
	private boolean isCursorDirectionForward = false;


	ModePredictive(SettingsStore settings, Language lang) {
		changeLanguage(lang);
		defaultTextCase();

		autoSpace = new AutoSpace(settings);
		autoTextCase = new AutoTextCase(settings);
		predictions = new Predictions();

		this.settings = settings;

		digitSequence = "";
	}


	@Override
	public boolean onBackspace() {
		isCursorDirectionForward = false;

		if (digitSequence.isEmpty()) {
			clearWordStem();
			return false;
		}

		digitSequence = digitSequence.substring(0, digitSequence.length() - 1);
		if (digitSequence.isEmpty()) {
			clearWordStem();
		} else if (stem.length() > digitSequence.length()) {
			stem = stem.substring(0, digitSequence.length());
		}

		return true;
	}


	@Override
	public boolean onNumber(int number, boolean hold, int repeat) {
		isCursorDirectionForward = true;

		if (hold) {
			// hold to type any digit
			reset();
			autoAcceptTimeout = 0;
			disablePredictions = true;
			digitSequence = String.valueOf(number);
			suggestions.add(language.getKeyNumber(number));
		} else {
			super.reset();
			digitSequence = EmojiLanguage.validateEmojiSequence(digitSequence, number);
			disablePredictions = false;

			if (digitSequence.equals(NaturalLanguage.PREFERRED_CHAR_SEQUENCE)) {
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
	 * Note that you need to manually get the suggestions again to obtain a filtered list.
	 */
	@Override
	public boolean setWordStem(String newStem, boolean exact) {
		if (newStem == null || newStem.isEmpty()) {
			if (stem.isEmpty()) {
				return false;
			}

			isStemFuzzy = false;
			stem = "";

			Logger.d(LOG_TAG, "Stem filter cleared");
			return true;
		}

		try {
			digitSequence = language.getDigitSequenceForWord(newStem);
			isStemFuzzy = !exact;
			stem = newStem.toLowerCase(language.getLocale());

			Logger.d(LOG_TAG, "Stem is now: " + stem + (isStemFuzzy ? " (fuzzy)" : ""));
			return true;
		} catch (Exception e) {
			isStemFuzzy = false;
			stem = "";

			Logger.w("setWordStem", "Ignoring invalid stem: " + newStem + " in language: " + language + ". " + e.getMessage());
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
	 * Loads the possible list of suggestions for the current digitSequence. "currentWord" is used
	 * for generating suggestions when there are no results.
	 * See: Predictions.generatePossibleCompletions()
	 */
	@Override
	public void loadSuggestions(Runnable onLoad, String currentWord) {
		if (disablePredictions) {
			super.loadSuggestions(onLoad, currentWord);
			return;
		}

		if (loadStaticSuggestions(onLoad)) {
			return;
		}

		Language searchLanguage = digitSequence.equals(EmojiLanguage.CUSTOM_EMOJI_SEQUENCE) ? new EmojiLanguage() : language;

		onSuggestionsUpdated = onLoad;
		predictions
			.setDigitSequence(digitSequence)
			.setIsStemFuzzy(isStemFuzzy)
			.setStem(stem)
			.setLanguage(searchLanguage)
			.setInputWord(currentWord)
			.setWordsChangedHandler(this::onPredictions)
			.load();
	}

	/**
	 * loadStatic
	 * Loads words that are not in the database and are supposed to be in the same order, such as
	 * emoji or the preferred character for double "0". Returns "false", when there are no static
	 * options for the current digitSequence.
	 */
	private boolean loadStaticSuggestions(Runnable onLoad) {
		if (digitSequence.equals(NaturalLanguage.PUNCTUATION_KEY) || digitSequence.equals(NaturalLanguage.SPECIAL_CHARS_KEY)) {
			super.loadSpecialCharacters(language);
			onLoad.run();
			return true;
		} else if (!digitSequence.equals(EmojiLanguage.CUSTOM_EMOJI_SEQUENCE) && digitSequence.startsWith(EmojiLanguage.EMOJI_SEQUENCE)) {
			suggestions.clear();
			suggestions.addAll(new EmojiLanguage().getKeyCharacters(digitSequence.charAt(0) - '0', digitSequence.length() - 2));
			onLoad.run();
			return true;
		} else if (digitSequence.startsWith(NaturalLanguage.PREFERRED_CHAR_SEQUENCE)) {
			suggestions.clear();
			suggestions.add(settings.getDoubleZeroChar());
			onLoad.run();
			return true;
		}

		return false;
	}


	/**
	 * onPredictions
	 * Gets the currently available Predictions and sends them over to the external caller.
	 */
	private void onPredictions() {
		// in case the user hasn't added any custom emoji, do not allow advancing to the empty character group
		if (predictions.getList().isEmpty() && digitSequence.startsWith(EmojiLanguage.EMOJI_SEQUENCE)) {
			digitSequence = EmojiLanguage.EMOJI_SEQUENCE;
			return;
		}

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
			Logger.i(LOG_TAG, "Current word is empty. Nothing to accept.");
			return;
		}

		// increment the frequency of the given word
		try {
			String sequence = language.getDigitSequenceForWord(currentWord);

			// emoji and punctuation are not in the database, so there is no point in
			// running queries that would update nothing
			if (!sequence.startsWith(NaturalLanguage.PUNCTUATION_KEY) && !sequence.startsWith(NaturalLanguage.SPECIAL_CHARS_KEY)) {
				WordStoreAsync.makeTopWord(language, currentWord, sequence);
			}
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Failed incrementing priority of word: '" + currentWord + "'. " + e.getMessage());
		}
	}


	@Override
	protected String adjustSuggestionTextCase(String word, int newTextCase) {
		return autoTextCase.adjustSuggestionTextCase(new Text(language, word), newTextCase);
	}

	@Override
	public void determineNextWordTextCase(String textBeforeCursor) {
		textCase = autoTextCase.determineNextWordTextCase(textCase, textFieldTextCase, textBeforeCursor);
	}

	@Override
	public int getTextCase() {
		// Filter out the internally used text cases. They have no meaning outside this class.
		return (textCase == CASE_UPPER || textCase == CASE_LOWER) ? textCase : CASE_CAPITALIZE;
	}

	@Override
	protected boolean nextSpecialCharacters() {
		return digitSequence.equals(NaturalLanguage.SPECIAL_CHARS_KEY) && super.nextSpecialCharacters();
	}

	@Override
	public boolean nextTextCase() {
		boolean changed = super.nextTextCase();
		textFieldTextCase = changed ? CASE_UNDEFINED : textFieldTextCase; // since it's a user's choice, the default matters no more
		return changed;
	}



	/**
	 * shouldAcceptPreviousSuggestion
	 * Automatic space assistance. Spaces (and special chars) cause suggestions to be accepted
	 * automatically. This is used for analysis before processing the incoming pressed key.
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
		// backspace never breaks words
		if (!isCursorDirectionForward) {
			return false;
		}

		// special characters always break words
		if (autoAcceptTimeout == 0 && !digitSequence.startsWith(NaturalLanguage.SPECIAL_CHARS_KEY)) {
			return true;
		}

		// allow apostrophes in the middle or at the end of Hebrew and Ukrainian words
		if (LanguageKind.isHebrew(language) || LanguageKind.isUkrainian(language)) {
			return
				predictions.noDbWords()
				&& digitSequence.equals(NaturalLanguage.PUNCTUATION_KEY);
		}

		// punctuation breaks words, unless there are database matches ('s, qu', по-, etc...)
		return
			!digitSequence.isEmpty()
			&& predictions.noDbWords()
			&& digitSequence.contains(NaturalLanguage.PUNCTUATION_KEY)
			&& !digitSequence.startsWith(EmojiLanguage.EMOJI_SEQUENCE)
			&& Text.containsOtherThan1(digitSequence);
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
