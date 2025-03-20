package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.AutoTextCase;
import io.github.sspanak.tt9.ime.modes.predictions.WordPredictions;
import io.github.sspanak.tt9.languages.EmojiLanguage;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
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
	private final AutoTextCase autoTextCase;
	private boolean isCursorDirectionForward = false;
	private int textFieldTextCase;


	protected ModeWords(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, inputType, textField);

		autoTextCase = new AutoTextCase(settings, inputType);

		changeLanguage(lang);
		defaultTextCase();
		determineTextFieldTextCase();
	}


	@Override protected void setCustomSpecialCharacters() {} // we use the default ones


	protected void setSpecialCharacterConstants() {
		PUNCTUATION_SEQUENCE = NaturalLanguage.PUNCTUATION_KEY;
		EMOJI_SEQUENCE = EmojiLanguage.EMOJI_SEQUENCE;
		CUSTOM_EMOJI_SEQUENCE = EmojiLanguage.CUSTOM_EMOJI_SEQUENCE;
		SPECIAL_CHAR_SEQUENCE = NaturalLanguage.SPECIAL_CHAR_KEY;
	}


	@Override
	protected void initPredictions() {
		predictions = new WordPredictions(settings, textField);
		predictions.setWordsChangedHandler(this::onPredictions);
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
		suggestions.add(language.getKeyNumeral(number));
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


	@Override
	public boolean recompose(String word) {
		if (!language.hasSpaceBetweenWords() || language.isSyllabary()) {
			return false;
		}

		if (word == null || word.length() < 2 || word.contains(" ")) {
			Logger.d(LOG_TAG, "Not recomposing invalid word: '" + word + "'");
			textCase = settings.getTextCase();
			return false;
		}

		try {
			reset();
			digitSequence = language.getDigitSequenceForWord(word);
			textCase = new Text(language, word).getTextCase();
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

		((WordPredictions) predictions)
			.setInputWord(currentWord.isEmpty() ? stem : currentWord)
			.setIsStemFuzzy(isStemFuzzy)
			.setStem(stem)
			.setDigitSequence(digitSequence)
			.setLanguage(shouldDisplayCustomEmojis() ? new EmojiLanguage() : language)
			.load();
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

		if (TextTools.isGraphic(currentWord) || new Text(currentWord).isNumeric()) {
			return;
		}

		try {
			// special chars are not in the database, no need to run queries on them
			String digitSequence = language.getDigitSequenceForWord(currentWord);
			if (digitSequence.equals(SPECIAL_CHAR_SEQUENCE) || digitSequence.equals(PUNCTUATION_SEQUENCE)) {
				return;
			}

			// increment the frequency of the given word
			predictions.onAccept(currentWord, digitSequence);
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Failed incrementing priority of word: '" + currentWord + "'. " + e.getMessage());
		}
	}


	@Override
	protected String adjustSuggestionTextCase(String word, int newTextCase) {
		return autoTextCase.adjustSuggestionTextCase(new Text(language, word), newTextCase);
	}

	@Override
	public void determineNextWordTextCase() {
		textCase = autoTextCase.determineNextWordTextCase(textCase, textFieldTextCase, textField.getStringBeforeCursor(), digitSequence);
	}

	private void determineTextFieldTextCase() {
		int fieldCase = inputType.determineTextCase();
		textFieldTextCase = allowedTextCases.contains(fieldCase) ? fieldCase : CASE_UNDEFINED;
	}

	@Override
	public int getTextCase() {
		// Filter out the internally used text cases. They have no meaning outside this class.
		return (textCase == CASE_UPPER || textCase == CASE_LOWER) ? textCase : CASE_CAPITALIZE;
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
	public boolean shouldReplaceLastLetter(int n, boolean h) {
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

		final char SPECIAL_CHAR_KEY_CODE = SPECIAL_CHAR_SEQUENCE.charAt(0);
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

		if (shouldContinueHebrewOrUkrainianWord(unacceptedText)) {
			return false;
		}

		// punctuation breaks words, unless there are database matches ('s, qu', по-, etc...)
		return
			!digitSequence.isEmpty()
			&& predictions.noDbWords()
			&& digitSequence.contains(PUNCTUATION_SEQUENCE)
			&& !digitSequence.startsWith(EMOJI_SEQUENCE)
			&& Text.containsOtherThan1(digitSequence);
	}


	/**
	 * Apostrophes never break Ukrainian and Hebrew words because they are used as letters. Same for
	 * the quotation marks in Hebrew.
	 */
	private boolean shouldContinueHebrewOrUkrainianWord(String unacceptedText) {
		if (unacceptedText.length() <= 1 || !predictions.noDbWords()) {
			return false;
		}

		if (LanguageKind.isHebrew(language)) {
			return new Text(language, unacceptedText).isValidWordWithPunctuation(List.of('"', '\''));
		}

		if (LanguageKind.isUkrainian(language)) {
			return new Text(language, unacceptedText).isValidWordWithPunctuation(List.of('\''));
		}

		return false;
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
