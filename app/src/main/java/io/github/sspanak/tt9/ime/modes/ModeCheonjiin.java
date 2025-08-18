package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.helpers.AutoSpace;
import io.github.sspanak.tt9.ime.modes.helpers.Cheonjiin;
import io.github.sspanak.tt9.ime.modes.helpers.Sequences;
import io.github.sspanak.tt9.ime.modes.predictions.Predictions;
import io.github.sspanak.tt9.ime.modes.predictions.SyllablePredictions;
import io.github.sspanak.tt9.languages.EmojiLanguage;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

class ModeCheonjiin extends InputMode {
	// used when we want do display a different set of characters for a given key, for example
	// in email fields
	protected final ArrayList<ArrayList<String>> KEY_CHARACTERS = new ArrayList<>();

	// special chars and emojis
	private final String PUNCTUATION_SEQUENCE_PREFIX = "11";
	private final String SPECIAL_CHAR_SEQUENCE_PREFIX = "00";

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

		autoSpace = new AutoSpace(settings);
		allowedTextCases.add(CASE_LOWER);
		digitSequence = "";
		seq = new Sequences(PUNCTUATION_SEQUENCE_PREFIX, SPECIAL_CHAR_SEQUENCE_PREFIX);
		this.inputType = inputType;
		this.textField = textField;

		setLanguage(LanguageCollection.getLanguage(LanguageKind.KOREAN));
		initPredictions();
	}


	/**
	 * setCustomSpecialCharacters
	 * Filter out the letters from the 0-key list and add "0", because there is no other way of
	 * typing it.
	 */
	protected void setCustomSpecialCharacters() {
		// special
		KEY_CHARACTERS.add(TextTools.removeLettersFromList(settings.getOrderedKeyChars(language, 0)));
		KEY_CHARACTERS.get(0).add(0, "0");

		// punctuation
		KEY_CHARACTERS.add(
			TextTools.removeLettersFromList(settings.getOrderedKeyChars(language, 1))
		);
	}


	@Override
	protected boolean setLanguage(@Nullable Language newLanguage) {
		if (!validateLanguage(newLanguage)) {
			return false;
		}

		super.setLanguage(newLanguage);

		autoSpace.setLanguage(language);

		KEY_CHARACTERS.clear();
		if (isEmailMode) {
			// Asian punctuation can not be used in email addresses, so we need to use the English locale.
			Language lang = LanguageKind.isCJK(language) ? LanguageCollection.getByLocale("en") : language;
			KEY_CHARACTERS.add(Characters.orderByList(Characters.Email.get(0), settings.getOrderedKeyChars(lang, 0), true));
			KEY_CHARACTERS.add(Characters.orderByList(Characters.Email.get(1), settings.getOrderedKeyChars(lang, 1), true));
		} else {
			setCustomSpecialCharacters();
		}

		return true;
	}


	protected boolean validateLanguage(Language newLanguage) {
		return LanguageKind.isKorean(newLanguage);
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
		if (digitSequence.equals(seq.CHARS_GROUP_1_SEQUENCE)) {
			digitSequence = seq.CHARS_1_SEQUENCE;
		} else if (digitSequence.equals(seq.CHARS_GROUP_0_SEQUENCE)) {
			digitSequence = seq.CHARS_0_SEQUENCE;
		} else if (digitSequence.equals(seq.CHARS_0_SEQUENCE) || digitSequence.equals(seq.CHARS_1_SEQUENCE) || (!digitSequence.startsWith(seq.CHARS_1_SEQUENCE) && Cheonjiin.isSingleJamo(digitSequence))) {
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
			digitSequence = seq.CHARS_0_SEQUENCE;
		} else if (number == 1) {
			disablePredictions = false;
			digitSequence = seq.CHARS_1_SEQUENCE;
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

		if (seq.startsWithEmojiSequence(digitSequence)) {
			digitSequence = EmojiLanguage.validateEmojiSequence(seq, digitSequence, nextNumber);
		} else if (!seq.CHARS_GROUP_0_SEQUENCE.equals(digitSequence) && !seq.CHARS_GROUP_1_SEQUENCE.equals(digitSequence)) {
			digitSequence += String.valueOf(nextNumber);
		}

		if (seq.PREFERRED_CHAR_SEQUENCE.equals(digitSequence)) {
			autoAcceptTimeout = 0;
		}
	}


	protected int shouldRewindRepeatingNumbers(int nextNumber) {
		if (seq.isAnySpecialCharSequence(digitSequence)) {
			return 0;
		}

		final int nextChar = nextNumber + '0';
		final int repeatingDigits = digitSequence.length() > 1 && digitSequence.charAt(digitSequence.length() - 1) == nextChar ? Cheonjiin.getRepeatingEndingDigits(digitSequence) : 0;
		final int keyCharsCount = nextNumber == 0 ? 2 : language.getKeyCharacters(nextNumber).size();

		if (repeatingDigits == 0 || keyCharsCount < 2) {
			return 0;
		}

		return keyCharsCount < repeatingDigits + 1 ? repeatingDigits : 0;
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
			predictions.reset();
			onSuggestionsUpdated.run();
			return;
		}

		String currentSeq = digitSequence;
		if (shouldDisplayCustomEmojis()) {
			currentSeq = digitSequence.substring(PUNCTUATION_SEQUENCE_PREFIX.length());
		} else if (!previousJamoSequence.isEmpty()) {
			currentSeq = previousJamoSequence;
		}

		predictions
			.setLanguage(shouldDisplayCustomEmojis() ? new EmojiLanguage(seq) : language)
			.setDigitSequence(currentSeq)
			.load();
	}


	protected boolean loadEmojis() {
		if (shouldDisplayEmojis()) {
			suggestions.clear();
			suggestions.addAll(new EmojiLanguage(seq).getKeyCharacters(digitSequence.charAt(digitSequence.length() - 1) - '0', getEmojiGroup()));
			return true;
		}

		return false;
	}


	protected int getEmojiGroup() {
		return digitSequence.length() - seq.EMOJI_SEQUENCE.length();
	}


	protected boolean shouldDisplayEmojis() {
		return !isEmailMode && digitSequence.startsWith(seq.EMOJI_SEQUENCE) && !digitSequence.equals(seq.CUSTOM_EMOJI_SEQUENCE);
	}


	protected boolean shouldDisplayCustomEmojis() {
		return !isEmailMode && digitSequence.equals(seq.CUSTOM_EMOJI_SEQUENCE);
	}


	@Override
	protected boolean loadSpecialCharacters() {
		if (!shouldDisplaySpecialCharacters()) {
			return false;
		}

		if (digitSequence.equals(seq.CHARS_0_SEQUENCE) || digitSequence.equals(seq.CHARS_1_SEQUENCE)) {
			int number = digitSequence.isEmpty() ? Integer.MAX_VALUE : digitSequence.charAt(digitSequence.length() - 1) - '0';
			if (KEY_CHARACTERS.size() > number) {
				suggestions.clear();
				suggestions.addAll(KEY_CHARACTERS.get(number));
				return true;
			}
		}

		return super.loadSpecialCharacters();
	}


	protected boolean shouldDisplaySpecialCharacters() {
		return
			digitSequence.equals(seq.CHARS_1_SEQUENCE)
			|| digitSequence.equals(seq.CHARS_0_SEQUENCE)
			|| digitSequence.equals(seq.CHARS_GROUP_1_SEQUENCE)
			|| digitSequence.equals(seq.CHARS_GROUP_0_SEQUENCE);
	}


	/**
	 * onPredictions
	 * Gets the currently available Predictions and sends them over to the external caller.
	 */
	protected void onPredictions() {
		// in case the user hasn't added any custom emoji, do not allow advancing to the empty character group
		if (predictions.getList().isEmpty() && digitSequence.startsWith(seq.EMOJI_SEQUENCE)) {
			digitSequence = seq.EMOJI_SEQUENCE;
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
	public boolean shouldAcceptPreviousSuggestion(String currentWord, int nextKey, boolean hold) {
		return
			(hold && !digitSequence.isEmpty())
			|| (nextKey != Sequences.CHARS_0_KEY && digitSequence.startsWith(seq.CHARS_0_SEQUENCE))
			|| (nextKey != Sequences.CHARS_1_KEY && digitSequence.startsWith(seq.CHARS_1_SEQUENCE));
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
