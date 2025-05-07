package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.AutoSpace;
import io.github.sspanak.tt9.ime.modes.helpers.Cheonjiin;
import io.github.sspanak.tt9.ime.modes.predictions.Predictions;
import io.github.sspanak.tt9.ime.modes.predictions.SyllablePredictions;
import io.github.sspanak.tt9.languages.EmojiLanguage;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

class ModeCheonjiin extends InputMode {
	// used when we want do display a different set of characters for a given key, for example
	// in email fields
	protected final ArrayList<ArrayList<String>> KEY_CHARACTERS = new ArrayList<>();

	// special chars and emojis
	private static String SPECIAL_CHAR_SEQUENCE_PREFIX;
	protected String CUSTOM_EMOJI_SEQUENCE;
	protected String EMOJI_SEQUENCE;
	protected String PUNCTUATION_SEQUENCE;
	protected String SPECIAL_CHAR_SEQUENCE;

	// predictions
	protected boolean disablePredictions = false;
	protected Predictions predictions;
	@NonNull private String previousJamoSequence = "";

	// text analysis
	protected final AutoSpace autoSpace;
	protected final InputType inputType;
	protected final TextField textField;


	protected ModeCheonjiin(SettingsStore settings, InputType inputType, TextField textField) {
		super(settings, inputType);

		SPECIAL_CHAR_SEQUENCE_PREFIX = "11";


		autoSpace = new AutoSpace(settings);
		digitSequence = "";
		allowedTextCases.add(CASE_LOWER);
		this.inputType = inputType;
		this.textField = textField;

		setLanguage(LanguageCollection.getLanguage(LanguageKind.KOREAN));
		initPredictions();
		setSpecialCharacterConstants();
	}


	/**
	 * setCustomSpecialCharacters
	 * Filter out the letters from the 0-key list and add "0", because there is no other way of
	 * typing it.
	 */
	protected void setCustomSpecialCharacters() {
		// special
		KEY_CHARACTERS.add(TextTools.removeLettersFromList(applyPunctuationOrder(Characters.getSpecial(language), 0)));
		KEY_CHARACTERS.get(0).add(0, "0");

		// punctuation
		KEY_CHARACTERS.add(
			TextTools.removeLettersFromList(applyPunctuationOrder(Characters.PunctuationKorean, 1))
		);
	}


	@Override
	protected void setLanguage(@Nullable Language newLanguage) {
		super.setLanguage(newLanguage);

		autoSpace.setLanguage(language);

		KEY_CHARACTERS.clear();
		if (isEmailMode) {
			KEY_CHARACTERS.add(applyPunctuationOrder(Characters.Email.get(0), 0));
			KEY_CHARACTERS.add(applyPunctuationOrder(Characters.Email.get(1), 1));
		} else {
			setCustomSpecialCharacters();
		}
	}


	protected void setSpecialCharacterConstants() {
		CUSTOM_EMOJI_SEQUENCE = SPECIAL_CHAR_SEQUENCE_PREFIX + EmojiLanguage.CUSTOM_EMOJI_SEQUENCE;
		EMOJI_SEQUENCE = SPECIAL_CHAR_SEQUENCE_PREFIX + EmojiLanguage.EMOJI_SEQUENCE;
		PUNCTUATION_SEQUENCE = SPECIAL_CHAR_SEQUENCE_PREFIX + NaturalLanguage.PUNCTUATION_KEY;
		SPECIAL_CHAR_SEQUENCE = "000";
	}


	protected void initPredictions() {
		predictions = new SyllablePredictions(settings);
		predictions
			.setOnlyExactMatches(true)
			.setMinWords(0)
			.setWordsChangedHandler(this::onPredictions);
	}


	@Override
	public boolean onBackspace() {
		if (digitSequence.equals(PUNCTUATION_SEQUENCE)) {
			digitSequence = "";
		} else if (digitSequence.equals(SPECIAL_CHAR_SEQUENCE) || (!digitSequence.startsWith(PUNCTUATION_SEQUENCE) && Cheonjiin.isSingleJamo(digitSequence))) {
			digitSequence = "";
		} else if (!digitSequence.isEmpty()) {
			digitSequence = digitSequence.substring(0, digitSequence.length() - 1);
		}

		return !digitSequence.isEmpty();
	}


	@Override
	public boolean onNumber(int number, boolean hold, int repeat) {
		if (hold) {
			reset();
			digitSequence = String.valueOf(number);
			disablePredictions = true;
			onNumberHold(number);
		} else {
			basicReset();
			disablePredictions = false;
			onNumberPress(number);
		}

		return true;
	}


	protected void onNumberHold(int number) {
		if (number == 0) {
			disablePredictions = false;
			digitSequence = SPECIAL_CHAR_SEQUENCE;
		} else if (number == 1) {
			disablePredictions = false;
			digitSequence = PUNCTUATION_SEQUENCE;
		} else {
			autoAcceptTimeout = 0;
			suggestions.add(language.getKeyNumeral(number));
		}
	}


	protected void onNumberPress(int nextNumber) {
		int rewindAmount = shouldRewindRepeatingNumbers(nextNumber);
		if (rewindAmount > 0) {
			digitSequence = digitSequence.substring(0, digitSequence.length() - rewindAmount);
		}

		if (digitSequence.startsWith(PUNCTUATION_SEQUENCE)) {
			digitSequence = SPECIAL_CHAR_SEQUENCE_PREFIX + EmojiLanguage.validateEmojiSequence(digitSequence.substring(SPECIAL_CHAR_SEQUENCE_PREFIX.length()), nextNumber);
		} else {
			digitSequence += String.valueOf(nextNumber);
		}
	}


	private int shouldRewindRepeatingNumbers(int nextNumber) {
		final int nextChar = nextNumber + '0';
		final int repeatingDigits = digitSequence.length() > 1 && digitSequence.charAt(digitSequence.length() - 1) == nextChar ? Cheonjiin.getRepeatingEndingDigits(digitSequence) : 0;
		final int keyCharsCount = nextNumber == 0 ? 2 : language.getKeyCharacters(nextNumber).size();

		if (SPECIAL_CHAR_SEQUENCE.equals(digitSequence)) {
			return SPECIAL_CHAR_SEQUENCE.length();
		}

		if (repeatingDigits == 0 || keyCharsCount < 2) {
			return 0;
		}

		return keyCharsCount < repeatingDigits + 1 ? repeatingDigits : 0;
	}


	@Override
	public boolean changeLanguage(@Nullable Language newLanguage) {
		return LanguageKind.isKorean(newLanguage);
	}


	@Override
	public void reset() {
		basicReset();
		digitSequence = "";
		previousJamoSequence = "";
		disablePredictions = false;
	}


	protected void basicReset() {
		super.reset();
	}


	@Override
	public void loadSuggestions(String ignored) {
		if (disablePredictions || loadSpecialCharacters() || loadEmojis()) {
			onSuggestionsUpdated.run();
			return;
		}

		String seq = digitSequence;
		if (shouldDisplayCustomEmojis()) {
			seq = digitSequence.substring(SPECIAL_CHAR_SEQUENCE_PREFIX.length());
		} else if (!previousJamoSequence.isEmpty()) {
			seq = previousJamoSequence;
		}

		predictions
			.setLanguage(shouldDisplayCustomEmojis() ? new EmojiLanguage() : language)
			.setDigitSequence(seq)
			.load();
	}


	protected boolean loadEmojis() {
		if (shouldDisplayEmojis()) {
			suggestions.clear();
			suggestions.addAll(new EmojiLanguage().getKeyCharacters(digitSequence.charAt(digitSequence.length() - 1) - '0', getEmojiGroup()));
			return true;
		}

		return false;
	}


	protected int getEmojiGroup() {
		return digitSequence.length() - EMOJI_SEQUENCE.length();
	}


	protected boolean shouldDisplayEmojis() {
		return !isEmailMode && digitSequence.startsWith(EMOJI_SEQUENCE) && !digitSequence.equals(CUSTOM_EMOJI_SEQUENCE);
	}


	protected boolean shouldDisplayCustomEmojis() {
		return !isEmailMode && digitSequence.equals(CUSTOM_EMOJI_SEQUENCE);
	}


	@Override
	protected boolean loadSpecialCharacters() {
		if (!shouldDisplaySpecialCharacters()) {
			return false;
		}

		// KEY_CHARACTERS contains chars for group 0 only. If it turns out we are at group > 0
		// we must allow super to do its job.
		boolean callSuper = true;
		if (specialCharSelectedGroup > 0) {
			super.loadSpecialCharacters(); // this increments specialCharSelectedGroup or resets it to 0, if no more groups are available
			callSuper = false;
			if (specialCharSelectedGroup > 0) {
				return true;
			}
		}

		// ... otherwise display our custom first groups, if available
		int number = digitSequence.isEmpty() ? Integer.MAX_VALUE : digitSequence.charAt(digitSequence.length() - 1) - '0';
		if (KEY_CHARACTERS.size() > number) {
			suggestions.clear();
			suggestions.addAll(KEY_CHARACTERS.get(number));
			return true;
		}

		// if we never asked super to advance the group and load the respective chars, do it now
		return callSuper && super.loadSpecialCharacters();
	}


	protected boolean shouldDisplaySpecialCharacters() {
		return digitSequence.equals(PUNCTUATION_SEQUENCE) || digitSequence.equals(SPECIAL_CHAR_SEQUENCE);
	}


	/**
	 * onPredictions
	 * Gets the currently available Predictions and sends them over to the external caller.
	 */
	protected void onPredictions() {
		// in case the user hasn't added any custom emoji, do not allow advancing to the empty character group
		if (predictions.getList().isEmpty() && digitSequence.startsWith(EMOJI_SEQUENCE)) {
			digitSequence = EMOJI_SEQUENCE;
			return;
		}

		suggestions.clear();
		suggestions.addAll(predictions.getList());

		onSuggestionsUpdated.run();
	}


	private void onReplacementPredictions() {
		autoAcceptTimeout = 0;
		onPredictions();
		predictions.setWordsChangedHandler(this::onPredictions);

		autoAcceptTimeout = -1;
		loadSuggestions(null);
	}


	@Override
	public boolean containsGeneratedSuggestions() {
		return predictions.containsGeneratedWords();
	}


	@Override
	public void replaceLastLetter() {
		previousJamoSequence = Cheonjiin.stripRepeatingEndingDigits(digitSequence);
		if (previousJamoSequence.isEmpty() || previousJamoSequence.length() == digitSequence.length()) {
			previousJamoSequence = "";
			return;
		}

		digitSequence = digitSequence.substring(previousJamoSequence.length());

		predictions.setWordsChangedHandler(this::onReplacementPredictions);
	}


	@Override
	public boolean shouldReplaceLastLetter(int nextKey, boolean hold) {
		return !hold && !shouldDisplayEmojis() && Cheonjiin.isThereMediaVowel(digitSequence) && Cheonjiin.isVowelDigit(nextKey);
	}


	/**
	 * shouldAcceptPreviousSuggestion
	 * Used for analysis before processing the incoming pressed key.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) {
		return
			(hold && !digitSequence.isEmpty())
			|| (digitSequence.equals(SPECIAL_CHAR_SEQUENCE) && nextKey != 0)
			|| (digitSequence.startsWith(PUNCTUATION_SEQUENCE) && nextKey != 1);
	}


	/**
	 * shouldAcceptPreviousSuggestion
	 * Used for analysis after loading the suggestions.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(String unacceptedText) {
		return
			!digitSequence.isEmpty()
			&& !disablePredictions && !shouldDisplayEmojis() && !shouldDisplaySpecialCharacters() && predictions.noDbWords()
			&& (Cheonjiin.endsWithDashVowel(digitSequence) || Cheonjiin.endsWithTwoConsonants(digitSequence));
	}


	@Override
	public void onAcceptSuggestion(@NonNull String word, boolean preserveWordList) {
		if (shouldDisplaySpecialCharacters() || shouldDisplayEmojis()) {
			reset();
			return;
		}

		String digitSequenceStash = "";

		boolean mustReload = false;
		if (predictions.noDbWords() && digitSequence.length() >= 2) {
			digitSequenceStash = digitSequence.substring(digitSequence.length() - 1);
			mustReload = true;
		} else if (!previousJamoSequence.isEmpty()) {
			digitSequenceStash = digitSequence;
		}

		reset();

		digitSequence = digitSequenceStash;
		if (mustReload) {
			loadSuggestions(null);
		}
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


	@Override
	public int getId() {
		return MODE_PREDICTIVE;
	}


	@NonNull
	@Override
	public String toString() {
		return language.getName();
	}
}
