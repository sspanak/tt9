package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.AutoTextCase;
import io.github.sspanak.tt9.ime.modes.helpers.Sequences;
import io.github.sspanak.tt9.ime.modes.predictions.WordPredictions;
import io.github.sspanak.tt9.languages.EmojiLanguage;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

class ModeWords extends ModeCheonjiin {
	private final String LOG_TAG = getClass().getSimpleName();

	private String lastAcceptedWord = "";

	// stem filter
	private boolean isStemFuzzy = false;
	protected String stem = "";

	// text analysis tools
	private final AutoTextCase autoTextCase;
	private boolean isCursorDirectionForward = false;
	private boolean isRecomposing = false;
	private int textFieldTextCase;


	protected ModeWords(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, inputType, textField);

		seq = new Sequences();
		autoTextCase = new AutoTextCase(settings, seq, inputType);

		setLanguage(lang);
		defaultTextCase();
		determineTextFieldTextCase();
	}

	@Override protected void setCustomSpecialCharacters() {} // use defaults


	@Override
	protected void initPredictions() {
		predictions = new WordPredictions(settings, textField, seq);
		predictions.setWordsChangedHandler(this::onPredictions);
	}


	@Override
	public boolean onBackspace() {
		isCursorDirectionForward = false;
		autoTextCase.doNotSkipNext();

		if (digitSequence.isEmpty()) {
			clearWordStem();
			return false;
		}

		if (digitSequence.equals(seq.CHARS_GROUP_1_SEQUENCE)) {
			digitSequence = seq.CHARS_1_SEQUENCE;
		} else if (digitSequence.equals(seq.CHARS_GROUP_0_SEQUENCE)) {
			digitSequence = seq.CHARS_0_SEQUENCE;
		} else {
			digitSequence = digitSequence.substring(0, digitSequence.length() - 1);
		}

		if (digitSequence.isEmpty()) {
			clearWordStem();
			endRecomposing();
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
	protected boolean setLanguage(@Nullable Language newLanguage) {
		if (!super.setLanguage(newLanguage)) {
			return false;
		}

		allowedTextCases.clear();
		allowedTextCases.add(CASE_LOWER);
		if (language.hasUpperCase()) {
			allowedTextCases.add(CASE_CAPITALIZE);
			allowedTextCases.add(CASE_UPPER);
		}

		return true;
	}


	@Override
	public boolean validateLanguage(@Nullable Language newLanguage) {
		return newLanguage != null && !newLanguage.isTranscribed();
	}


	@Override
	public String recompose() {
		isRecomposing = false;
		if (!language.hasSpaceBetweenWords() || language.isTranscribed()) {
			return null;
		}

		String after = textField.getStringAfterCursor(1);
		if (!after.isEmpty() && !Character.isWhitespace(after.codePointAt(0))) {
			return null;
		}

		boolean includeApostrophes = LanguageKind.isUkrainian(language) || LanguageKind.isHebrew(language);
		String previousWord = textField.getTextBeforeCursor().getPreviousWord(false, includeApostrophes);
		if (previousWord.length() < 2 || previousWord.contains(" ")) {
			Logger.d(LOG_TAG, "Not recomposing invalid word: '" + previousWord + "'");
			textCase = settings.getTextCase();
			return null;
		}

		try {
			reset();
			digitSequence = language.getDigitSequenceForWord(previousWord);
			textCase = new Text(language, previousWord).getTextCase();
			isRecomposing = true;
		} catch (InvalidLanguageCharactersException e) {
			Logger.d(LOG_TAG, "Not recomposing word: '" + previousWord + "'. " + e.getMessage());
			return null;
		}

		return previousWord;
	}

	private void endRecomposing() {
		if (isRecomposing) {
			isRecomposing = false;
			textCase = settings.getTextCase();
		}
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
			digitSequence = Characters.getWhitespaces(language).contains(newStem) ? seq.CHARS_0_SEQUENCE : language.getDigitSequenceForWord(newStem);
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


	@Override
	public boolean supportsFiltering() {
		return true;
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
			.setLanguage(shouldDisplayCustomEmojis() ? new EmojiLanguage(seq) : language)
			.load();
	}


	protected boolean loadPreferredChar() {
		if (digitSequence.equals(seq.PREFERRED_CHAR_SEQUENCE)) {
			suggestions.clear();
			suggestions.add(getPreferredChar());
			return true;
		}

		return false;
	}


	protected String getPreferredChar() {
		return settings.getDoubleZeroChar();
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
			endRecomposing();
		}
		stem = "";

		if (currentWord.isEmpty() || !language.isValidWord(currentWord)) {
			Logger.i(LOG_TAG, "Current word is empty or invalid. Nothing to accept.");
			return;
		}

		if (TextTools.isGraphic(currentWord) || new Text(currentWord).isNumeric()) {
			return;
		}

		try {
			// special chars are not in the database, no need to run queries on them
			String currentWordSeq = language.getDigitSequenceForWord(currentWord);
			if (seq.isAnySpecialCharSequence(currentWordSeq)) {
				return;
			}

			// increment the frequency of the given word
			predictions.onAccept(currentWord, currentWordSeq);
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Failed incrementing priority of word: '" + currentWord + "'. " + e.getMessage());
		}
	}


	@Override
	protected String adjustSuggestionTextCase(String word, int newTextCase) {
		return autoTextCase.adjustSuggestionTextCase(new Text(language, word), newTextCase);
	}

	@Override
	public void determineNextWordTextCase(int nextDigit) {
		final String nextSequence = nextDigit >= 0 ? digitSequence + nextDigit : digitSequence;
		textCase = autoTextCase.determineNextWordTextCase(language, textCase, textFieldTextCase, textField.getStringBeforeCursor(), nextSequence);
	}

	private void determineTextFieldTextCase() {
		int fieldCase = inputType.determineTextCase();
		textFieldTextCase = allowedTextCases.contains(fieldCase) ? fieldCase : CASE_UNDEFINED;
	}

	@Override
	public int getTextCase() {
		// Filter out the internally used text cases. They have no meaning outside this class.
		return switch (textCase) {
			case CASE_UPPER, CASE_CAPITALIZE -> textCase;
			case CASE_DICTIONARY -> CASE_CAPITALIZE;
			default -> CASE_LOWER;
		};
	}

	@Override
	public boolean nextTextCase(@Nullable String currentWord, int displayTextCase) {
		if (!language.hasUpperCase()) {
			return false;
		}

		boolean isTyping = currentWord != null && !currentWord.isEmpty();
		boolean isTyingSpecialChar = isTyping && currentWord.length() == 1 && !Character.isAlphabetic(currentWord.charAt(0));

		if (isTyingSpecialChar) {
			textCase = displayTextCase;
		} else if (isTyping) {
			textCase = new Text(language, currentWord).getTextCase();
		} else {
			textCase = getTextCase();
		}

		// do not capitalize words like: 've, 's, 'll, etc, only allow upper and lower cases.
		boolean changed = super.nextTextCase(currentWord, displayTextCase);
		if (textCase != CASE_LOWER && textCase != CASE_UPPER && currentWord != null && currentWord.length() > 1 && !Character.isAlphabetic(currentWord.charAt(0))) {
			changed = super.nextTextCase(currentWord, displayTextCase);
		}

		// since the user made an explicit choice, the app default matters no more
		textFieldTextCase = changed ? CASE_UNDEFINED : textFieldTextCase;

		return changed;
	}

	@Override
	public void skipNextTextCaseDetection() {
		autoTextCase.skipNext();
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
	public boolean shouldAcceptPreviousSuggestion(String currentWord, int nextDigit, boolean hold) {
		if (hold) {
			return true;
		}

		if (digitSequence.isEmpty()) {
			return false;
		} else if (
			digitSequence.equals(seq.CUSTOM_EMOJI_SEQUENCE) ||
			(seq.startsWithEmojiSequence(digitSequence) && nextDigit != Sequences.CHARS_1_KEY && nextDigit != Sequences.CUSTOM_EMOJI_KEY)
		) {
			return true;
		}

		// Prevent typing the preferred character when the user has scrolled the special char suggestions.
		// For example, it makes more sense to allow typing "+ " with 0 + scroll + 0, instead of clearing
		// the "+" and replacing it with the preferred character.
		// Also don't type the preferred character when viewing a group. In that case we obviously want to
		// type a space after the character from the group.
		boolean inGroup = digitSequence.equals(seq.CHARS_GROUP_0_SEQUENCE) || digitSequence.equals(seq.CHARS_GROUP_1_SEQUENCE);
		boolean isWhitespaceAndScrolled = digitSequence.equals(seq.CHARS_0_SEQUENCE) && !suggestions.isEmpty() && !suggestions.get(0).equals(currentWord);
		if (nextDigit == Sequences.CHARS_0_KEY && (isWhitespaceAndScrolled || inGroup)) {
			return true;
		}

		final char lastDigit = digitSequence.charAt(digitSequence.length() - 1);

		return
			(nextDigit == Sequences.CHARS_0_KEY && lastDigit != Sequences.CHARS_0_CODE)
			|| (nextDigit != Sequences.CHARS_0_KEY && lastDigit == Sequences.CHARS_0_CODE);
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

		return
			!digitSequence.isEmpty()
			&& predictions.noDbWords()
			&& (
				// when no custom emoji, assume the last digit is the beginning of a new word
				digitSequence.equals(seq.CUSTOM_EMOJI_SEQUENCE)
				// punctuation breaks words, unless there are database matches ('s, qu', по-, etc...)
				|| (digitSequence.contains(seq.CHARS_1_SEQUENCE) && !digitSequence.equals(seq.CHARS_1_SEQUENCE))
			);
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


	@Override protected int shouldRewindRepeatingNumbers(int nextNumber) { return 0; }


	@NonNull
	@Override
	public String toString() {
		String modeString = language.getName();
		if (textCase == CASE_UPPER) {
			return modeString.toUpperCase(language.getLocale());
		} else if (textCase == CASE_LOWER) {
			return modeString.toLowerCase(language.getLocale());
		} else {
			return modeString;
		}
	}
}
