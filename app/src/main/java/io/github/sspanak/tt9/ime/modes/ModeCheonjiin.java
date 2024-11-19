package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.modes.helpers.Cheonjiin;
import io.github.sspanak.tt9.ime.modes.predictions.Predictions;
import io.github.sspanak.tt9.ime.modes.predictions.SyllablePredictions;
import io.github.sspanak.tt9.languages.EmojiLanguage;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Characters;

public class ModeCheonjiin extends InputMode {
	private static final String LOG_TAG = ModeCheonjiin.class.getSimpleName();

	private static final String SPECIAL_CHAR_SEQUENCE_PREFIX = "1";
	private static final String PUNCTUATION_SEQUENCE = SPECIAL_CHAR_SEQUENCE_PREFIX + NaturalLanguage.PUNCTUATION_KEY;
	private static final String EMOJI_SEQUENCE = SPECIAL_CHAR_SEQUENCE_PREFIX + EmojiLanguage.EMOJI_SEQUENCE;
	private static final String CUSTOM_EMOJI_SEQUENCE = SPECIAL_CHAR_SEQUENCE_PREFIX + EmojiLanguage.CUSTOM_EMOJI_SEQUENCE;
	private static final String SPECIAL_CHAR_SEQUENCE = "000";

	private final ArrayList<ArrayList<String>> KEY_CHARACTERS = new ArrayList<>();

	protected boolean disablePredictions = false;
	protected Predictions predictions;

	@NonNull private String previousJamoSequence = "";


	protected ModeCheonjiin(SettingsStore settings, InputType inputType) {
		super(settings);
		setLanguage(LanguageCollection.getLanguage(LanguageKind.KOREAN));
		allowedTextCases.add(CASE_LOWER);


		predictions = new SyllablePredictions(settings);
		predictions
			.setLanguage(language)
			.setOnlyExactMatches(true)
			.setMinWords(0)
			.setWordsChangedHandler(this::onPredictions);

		if (inputType.isEmail()) {
			KEY_CHARACTERS.add(applyPunctuationOrder(Characters.Email.get(0), 0));
			KEY_CHARACTERS.add(applyPunctuationOrder(Characters.Email.get(1), 1));
		}
	}


	@Override
	public boolean onBackspace() {
		if (digitSequence.equals(SPECIAL_CHAR_SEQUENCE)) {
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
			suggestions.add(language.getKeyNumber(number));
		} else {
			autoAcceptTimeout = 0;
			suggestions.add(language.getKeyNumber(number));
		}
	}


	protected void onNumberPress(int number) {
		int rewindAmount = shouldRewindRepeatingNumbers(number);
//		Logger.d(LOG_TAG, "=======> Rewind amount: " + rewindAmount);
		if (rewindAmount > 0) {
			digitSequence = digitSequence.substring(0, digitSequence.length() - rewindAmount);
		}

		if (digitSequence.startsWith(PUNCTUATION_SEQUENCE)) {
			digitSequence = "1" + EmojiLanguage.validateEmojiSequence(digitSequence.substring(0, digitSequence.length() - 1), number);
		} else {
			digitSequence += String.valueOf(number);
		}

//		Logger.d(LOG_TAG, "=======> digitSequence: " + digitSequence);
	}


	private int shouldRewindRepeatingNumbers(int nextNumber) {
		final int nextChar = nextNumber + '0';
		final int repeatingDigits = digitSequence.length() > 1 && digitSequence.charAt(digitSequence.length() - 1) == nextChar ? Cheonjiin.getRepeatingEndingDigits(digitSequence) : 0;
		final int keyCharsCount = nextNumber == 0 ? 2 : language.getKeyCharacters(nextNumber).size();

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
			removeLettersFromSpecialCharList();
			onSuggestionsUpdated.run();
			return;
		}

		String seq = previousJamoSequence.isEmpty() ? digitSequence : previousJamoSequence;
//		Logger.d(LOG_TAG, "=========> Loading suggestions for: " + seq);

		predictions
			.setLanguage(shouldDisplayCustomEmojis() ? new EmojiLanguage() : language)
			.setDigitSequence(seq)
			.load();
	}


	private void removeLettersFromSpecialCharList() {
		ArrayList<String> specialChars = new ArrayList<>();
		for (String s : suggestions) {
			if (!Character.isAlphabetic(s.codePointAt(0))) {
				specialChars.add(s);
			}
		}

		suggestions.clear();
		suggestions.addAll(specialChars);
	}


	protected boolean loadEmojis() {
		if (shouldDisplayEmojis()) {
			suggestions.clear();
			suggestions.addAll(new EmojiLanguage().getKeyCharacters(digitSequence.charAt(0) - '0', getEmojiGroup()));
			return true;
		}

		return false;
	}


	protected int getEmojiGroup() {
		return digitSequence.length() - 3;
	}


	protected boolean shouldDisplayEmojis() {
		return digitSequence.startsWith(EMOJI_SEQUENCE);
	}


	private boolean shouldDisplayCustomEmojis() {
		return digitSequence.equals(CUSTOM_EMOJI_SEQUENCE);
	}


	@Override
	protected boolean loadSpecialCharacters() {
		if (!shouldDisplaySpecialCharacters()) {
			return false;
		}

		int number = digitSequence.isEmpty() ? Integer.MAX_VALUE : digitSequence.charAt(0) - '0';
		if (KEY_CHARACTERS.size() > number) {
			suggestions.clear();
			suggestions.addAll(KEY_CHARACTERS.get(number));
			return true;
		} else {
			return super.loadSpecialCharacters();
		}
	}


	protected boolean shouldDisplaySpecialCharacters() {
		return digitSequence.equals(PUNCTUATION_SEQUENCE) || digitSequence.equals(SPECIAL_CHAR_SEQUENCE);
	}


	/**
	 * onPredictions
	 * Gets the currently available WordPredictions and sends them over to the external caller.
	 */
	protected void onPredictions() {
		suggestions.clear();
		suggestions.addAll(predictions.getList());

		onSuggestionsUpdated.run();
	}


	private void onReplacementPredictions() {
//		Logger.d(LOG_TAG, "=========> Replacement predictions for: " + digitSequence + " -> " + predictions.getList());
		autoAcceptTimeout = 0;
		onPredictions();
		predictions.setWordsChangedHandler(this::onPredictions);

//		Logger.d(LOG_TAG, "========> replacement predictions sent. Loading next. ");
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
//			Logger.w(LOG_TAG, "Cannot strip ending consonant digits from: " + digitSequence + ". Preserving the original sequence and suggestions.");
			return;
		}

		digitSequence = digitSequence.substring(previousJamoSequence.length());

//		Logger.d(LOG_TAG, "=======> previousCharSequence: " + previousJamoSequence + " || digitSequence: " + digitSequence);

		predictions.setWordsChangedHandler(this::onReplacementPredictions);
	}


	@Override
	public boolean shouldReplaceLastLetter(int nextKey) {
		boolean yes = Cheonjiin.isThereMediaVowel(digitSequence) && Cheonjiin.isVowelDigit(nextKey);
//		Logger.d(LOG_TAG, "========+> is there medial vowel:" + Cheonjiin.isThereMediaVowel(digitSequence) + " + is vowel digit: " + Cheonjiin.isVowelDigit(nextKey));
//
//		if (yes) {
//			Logger.d(LOG_TAG, "========+> should preserve last consonant: " + digitSequence + " + " + nextKey);
//		}

		return yes;
	}


	/**
	 * shouldAcceptPreviousSuggestion
	 * Used for analysis before processing the incoming pressed key.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) {
		return hold && !digitSequence.isEmpty();
	}


	/**
	 * shouldAcceptPreviousSuggestion
	 * Used for analysis after loading the suggestions.
	 */
	@Override
	public boolean shouldAcceptPreviousSuggestion(String unacceptedText) {
		return
			!digitSequence.isEmpty()
			&& !disablePredictions && !shouldDisplayEmojis() && predictions.noDbWords()
			&& (Cheonjiin.endsWithDashVowel(digitSequence) || Cheonjiin.endsWithTwoConsonants(digitSequence));
	}


	@Override
	public void onAcceptSuggestion(@NonNull String word, boolean preserveWordList) {
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
	protected boolean nextSpecialCharacters() {
		// @todo: This messes up the character order. Sort it out without breaking the descendants.
		return super.nextSpecialCharacters();
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
