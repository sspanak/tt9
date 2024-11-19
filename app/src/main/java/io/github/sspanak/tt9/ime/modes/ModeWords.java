package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.AutoSpace;
import io.github.sspanak.tt9.ime.modes.helpers.AutoTextCase;
import io.github.sspanak.tt9.ime.modes.predictions.WordPredictions;
import io.github.sspanak.tt9.languages.EmojiLanguage;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Characters;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.TextTools;

class ModeWords extends ModeCheonjiin {
	private final String LOG_TAG = getClass().getSimpleName();

	private String lastAcceptedWord = "";

	// stem filter
	private boolean isStemFuzzy = false;
	private String stem = "";

	// text analysis tools
	private final AutoSpace autoSpace;
	private final AutoTextCase autoTextCase;
	private boolean isCursorDirectionForward = false;
	private final InputType inputType;
	private final TextField textField;
	private int textFieldTextCase;


	protected ModeWords(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, inputType);

		autoSpace = new AutoSpace(settings).setLanguage(lang);
		autoTextCase = new AutoTextCase(settings);
		digitSequence = "";
		predictions = new WordPredictions(settings, textField);
		predictions.setWordsChangedHandler(this::onPredictions);
		this.inputType = inputType;
		this.textField = textField;

		changeLanguage(lang);
		defaultTextCase();
		determineTextFieldTextCase();
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
		return super.onNumber(number, hold, repeat);
	}


	@Override
	protected void onNumberHold(int number) {
		autoAcceptTimeout = 0;
		suggestions.add(language.getKeyNumber(number));
	}


	@Override
	protected void onNumberPress(int number) {
		digitSequence = EmojiLanguage.validateEmojiSequence(digitSequence, number);

		if (digitSequence.equals(NaturalLanguage.PREFERRED_CHAR_SEQUENCE)) {
			autoAcceptTimeout = 0;
		}
	}


	@Override
	public boolean changeLanguage(@Nullable Language newLanguage) {
		if (newLanguage != null && newLanguage.isSyllabary()) {
			return false;
		}

		super.setLanguage(newLanguage);

		autoSpace.setLanguage(language);

		allowedTextCases.clear();
		allowedTextCases.add(CASE_LOWER);
		if (language.hasUpperCase()) {
			allowedTextCases.add(CASE_CAPITALIZE);
			allowedTextCases.add(CASE_UPPER);
		}

		return true;
	}


	private void determineTextFieldTextCase() {
		int fieldCase = inputType.determineTextCase();
		textFieldTextCase = allowedTextCases.contains(fieldCase) ? fieldCase : CASE_UNDEFINED;
	}


	@Override
	public boolean recompose(String word) {
		if (!language.hasSpaceBetweenWords() || language.isSyllabary()) {
			return false;
		}

		if (word == null || word.length() < 2 || word.contains(" ")) {
			Logger.d(LOG_TAG, "Not recomposing invalid word: '" + word + "'");
			textCase = CASE_CAPITALIZE;
			return false;
		}

		try {
			reset();
			digitSequence = language.getDigitSequenceForWord(word);
			textCase = new Text(language, word).getTextCase();
			setWordStem(word,  true);
		} catch (InvalidLanguageCharactersException e) {
			Logger.d(LOG_TAG, "Not recomposing word: '" + word + "'. " + e.getMessage());
			return false;
		}

		return true;
	}

	@Override
	public void reset() {
		basicReset();
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
		stem = stem.length() > lastAcceptedWordLength ? stem.substring(lastAcceptedWordLength) : "";

		if (digitSequence.length() == 1) {
			suggestions.clear();
			loadSuggestions("");
			return;
		}

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
	 * See: WordPredictions.generatePossibleCompletions()
	 */
	@Override
	public void loadSuggestions(String currentWord) {
		if (disablePredictions || loadPreferredChar() || loadSpecialCharacters() || loadEmojis()) {
			onSuggestionsUpdated.run();
			return;
		}

		Language searchLanguage = digitSequence.equals(EmojiLanguage.CUSTOM_EMOJI_SEQUENCE) ? new EmojiLanguage() : language;

		((WordPredictions) predictions)
			.setIsStemFuzzy(isStemFuzzy)
			.setStem(stem)
			.setInputWord(currentWord.isEmpty() ? stem : currentWord)
			.setDigitSequence(digitSequence)
			.setLanguage(searchLanguage)
			.load();
	}


	@Override
	protected boolean shouldDisplayEmojis() {
		return !digitSequence.equals(EmojiLanguage.CUSTOM_EMOJI_SEQUENCE) && digitSequence.startsWith(EmojiLanguage.EMOJI_SEQUENCE);
	}


	@Override
	protected int getEmojiGroup() {
		return digitSequence.length() - 2;
	}


	@Override
	protected boolean shouldDisplaySpecialCharacters() {
		return digitSequence.equals(NaturalLanguage.PUNCTUATION_KEY) || digitSequence.equals(NaturalLanguage.SPECIAL_CHAR_KEY);
	}


	private boolean loadPreferredChar() {
		if (digitSequence.startsWith(NaturalLanguage.PREFERRED_CHAR_SEQUENCE)) {
			suggestions.clear();
			suggestions.add(settings.getDoubleZeroChar());
			return true;
		}

		return false;
	}


	/**
	 * onPredictions
	 * Gets the currently available WordPredictions and sends them over to the external caller.
	 */
	protected void onPredictions() {
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

		if (Characters.isStaticEmoji(currentWord)) {
			return;
		}


		// increment the frequency of the given word
		try {
			Language workingLanguage = TextTools.isGraphic(currentWord) ? new EmojiLanguage() : language;
			String sequence = workingLanguage.getDigitSequenceForWord(currentWord);

			// punctuation and special chars are not in the database, so there is no point in
			// running queries that would update nothing
			if (!sequence.equals(NaturalLanguage.PUNCTUATION_KEY) && !sequence.startsWith(NaturalLanguage.SPECIAL_CHAR_KEY)) {
				predictions.onAccept(currentWord, sequence);
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
		textCase = autoTextCase.determineNextWordTextCase(textCase, textFieldTextCase, textBeforeCursor, digitSequence);
	}

	@Override
	public int getTextCase() {
		// Filter out the internally used text cases. They have no meaning outside this class.
		return (textCase == CASE_UPPER || textCase == CASE_LOWER) ? textCase : CASE_CAPITALIZE;
	}

	@Override
	protected boolean shouldSelectNextSpecialCharacters() {
		return digitSequence.equals(NaturalLanguage.SPECIAL_CHAR_KEY);
	}

	@Override
	public boolean nextTextCase() {
		int before = textCase;
		boolean changed = super.nextTextCase();

		// When Auto Text Case is on, only upper- and automatic cases are available, so we skip lowercase.
		// Yet, we allow adjusting individual words to lowercase, if needed.
		if (digitSequence.isEmpty() && settings.getAutoTextCase() && language.hasUpperCase() && (before == CASE_LOWER || textCase == CASE_LOWER)) {
			changed = super.nextTextCase();
		}

		// since it's a user's choice, the default matters no more
		textFieldTextCase = changed ? CASE_UNDEFINED : textFieldTextCase;

		return changed;
	}


	@Override
	public boolean shouldReplaceLastLetter(int nextKey) {
		return false;
	}


	/**
	 * shouldAcceptPreviousSuggestion
	 * Automatic space assistance. Spaces (and special chars) cause suggestions to be accepted
	 * automatically. This is used for analysis before processing the incoming pressed key.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) {
		if (hold) {
			return true;
		}

		final char SPECIAL_CHAR_KEY_CODE = NaturalLanguage.SPECIAL_CHAR_KEY.charAt(0);
		final int SPECIAL_CHAR_KEY = SPECIAL_CHAR_KEY_CODE - '0';

		// Prevent typing the preferred character when the user has scrolled the special char suggestions.
		// For example, it makes more sense to allow typing "+ " with 0 + scroll + 0, instead of clearing
		// the "+" and replacing it with the preferred character.
		if (!stem.isEmpty() && nextKey == SPECIAL_CHAR_KEY && digitSequence.charAt(0) == SPECIAL_CHAR_KEY_CODE) {
			return true;
		}

		return
			!digitSequence.isEmpty() && (
				(nextKey == SPECIAL_CHAR_KEY && digitSequence.charAt(digitSequence.length() - 1) != SPECIAL_CHAR_KEY_CODE)
				|| (nextKey != SPECIAL_CHAR_KEY && digitSequence.charAt(digitSequence.length() - 1) == SPECIAL_CHAR_KEY_CODE)
			);
	}


	/**
	 * shouldAcceptPreviousSuggestion
	 * Used for analysis after loading the suggestions.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(String unacceptedText) {
		// backspace never breaks words
		if (!isCursorDirectionForward) {
			return false;
		}

		if (shouldAcceptHebrewOrUkrainianWord(unacceptedText)) {
			return true;
		}

		// punctuation breaks words, unless there are database matches ('s, qu', по-, etc...)
		return
			!digitSequence.isEmpty()
			&& predictions.noDbWords()
			&& digitSequence.contains(NaturalLanguage.PUNCTUATION_KEY)
			&& !digitSequence.startsWith(EmojiLanguage.EMOJI_SEQUENCE)
			&& Text.containsOtherThan1(digitSequence);
	}


	/**
	 * Apostrophes never break Ukrainian and Hebrew words because they are used as letters. Same for
	 * the quotation marks in Hebrew.
	 */
	private boolean shouldAcceptHebrewOrUkrainianWord(String unacceptedText) {
		char penultimateChar = unacceptedText.length() > 1 ? unacceptedText.charAt(unacceptedText.length() - 2) : 0;

		if (LanguageKind.isHebrew(language) && predictions.noDbWords()) {
			return penultimateChar != '\'' && penultimateChar != '"';
		}

		if (LanguageKind.isUkrainian(language) && predictions.noDbWords()) {
			return penultimateChar != '\'';
		}

		return false;
	}


	@Override
	public boolean shouldAddTrailingSpace(boolean isWordAcceptedManually, int nextKey) {
		return autoSpace.shouldAddTrailingSpace(textField, inputType, isWordAcceptedManually, nextKey);
	}


	@Override
	public boolean shouldAddPrecedingSpace() {
		return autoSpace.shouldAddBeforePunctuation(inputType, textField);
	}


	@Override
	public boolean shouldDeletePrecedingSpace() {
		return autoSpace.shouldDeletePrecedingSpace(inputType, textField);
	}


	@NonNull
	@Override
	public String toString() {
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
