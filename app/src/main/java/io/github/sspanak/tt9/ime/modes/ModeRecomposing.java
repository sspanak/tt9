package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;

public class ModeRecomposing extends InputMode {
	private final static String LOG_TAG = ModeRecomposing.class.getSimpleName();
	private final static int END_OF_WORD = -1;

	@Nullable private Runnable onFinishListener = null;
	@Nullable TextField textField;


	private int numberAtPosition = END_OF_WORD;
	private int previousNumberAtPosition = END_OF_WORD;
	private int position = 0;
	@NonNull private String prefix = "";
	@NonNull private String suffix = "";
	private int suggestionRecommendation = 0;
	private String wordAfterBackspace = null;



	@Override public int getId() { return MODE_RECOMPOSING; }


	protected ModeRecomposing(@NonNull SettingsStore settings, @NonNull Language lang, @Nullable InputType inputType, @Nullable TextField textField) {
		super(settings, inputType);
		this.textField = textField;
		setLanguage(lang);
	}


	@Override
	public void reset() {
		resetWithoutSelfDestruct();
		if (onFinishListener != null) {
			onFinishListener.run();
		}
	}


	private void resetWithoutSelfDestruct() {
		super.reset();
		digitSequence = "";
		numberAtPosition = END_OF_WORD;
		previousNumberAtPosition = END_OF_WORD;
		position = 0;
		prefix = "";
		suffix = "";
		suggestionRecommendation = 0;
		textCase = CASE_LOWER;
		wordAfterBackspace = null;
	}


	@Override
	protected boolean setLanguage(@Nullable Language newLanguage) {
		resetWithoutSelfDestruct();

		if (newLanguage != null && newLanguage.isTranscribed()) {
			return false;
		}

		super.setLanguage(newLanguage);

		allowedTextCases.clear();
		allowedTextCases.add(CASE_LOWER);
		if (language.hasUpperCase()) {
			allowedTextCases.add(CASE_UPPER);
		}

		return true;
	}


	public void setOnFinishListener(@Nullable Runnable listener) {
		onFinishListener = listener;
	}


	@Override
	public boolean onBackspace() {
		final int deletePosition = Math.min(position, digitSequence.length() - 1);
		final String word = textField == null ? "" : textField.getComposingText();

		wordAfterBackspace = new Text(language, word).deleteCharAt(deletePosition);
		final String newSequence = new Text(language, digitSequence).deleteCharAt(deletePosition);

		if (wordAfterBackspace.isEmpty() || newSequence.isEmpty()) {
			Logger.d(LOG_TAG, "Recomposing cancelled because the word is completely deleted with backspace.");
			finish();
			return false;
		}

		int newPosition = Math.min(newSequence.length() - 1, position);

		// order does matter here
		digitSequence = newSequence;
		position = newPosition;
		numberAtPosition = getCurrentSequenceNumber();

		// here, order is irrelevant
		suggestions.clear();

		if (suffix.isEmpty() && !prefix.isEmpty()) {
			prefix = prefix.substring(0, prefix.length() - 1);
		} else {
			suffix = new Text(language, suffix).deleteCharAt(0);
		}

		return true;
	}


	@Override
	public void onCursorMove(@NonNull String w) {
		finish();
	}


	@Override
	public boolean onNumber(int number, boolean hold, int repeat, @NonNull String[] surroundingChars) {
		wordAfterBackspace = null;
		numberAtPosition = number;
		updateDigitSequence();
		return true;
	}


	public void duplicateLetter() {
		if (digitSequence.isEmpty()) {
			Logger.d(LOG_TAG, "Cannot duplicate a letter for an empty word.");
			return;
		}

		digitSequence = new Text(digitSequence).duplicateCharAt(position);

		final String word = textField != null ? textField.getComposingText() : "";
		prefix += position < word.length() ? word.charAt(position) : "";
		wordAfterBackspace = new Text(language, word).duplicateCharAt(position);

		position++;
		suggestions.clear();

		loadSuggestions("");
	}


	@Override
	public void loadSuggestions(String w) {
		String currentWord = textField == null ? "" : textField.getComposingText();

		if (wordAfterBackspace != null) {
			currentWord = wordAfterBackspace;
			wordAfterBackspace = null;
		}

		if (currentWord.isEmpty() || digitSequence.isEmpty()) {
			Logger.d(LOG_TAG, "Cannot recompose an empty word. Use setWordStem() first.");
			suggestions.clear();
		} else if (numberAtPosition == END_OF_WORD) {
			resetWithoutSelfDestruct();
		} else if (numberAtPosition == 0) { // Space aborts the process
			finish();
			return;
		} else {
			suggestions.clear();
			loadLettersForPosition(currentWord, CASE_UNDEFINED);
			loadSuffixForPosition(currentWord);
		}

		super.loadSuggestions(w);
	}


	@Override
	public boolean setWordStem(String stem, boolean scrolling) {
		if (!scrolling) {
			resetWithoutSelfDestruct();
		}

		if (scrolling || stem == null || stem.isEmpty()) {
			return false;
		}

		final String lowerCaseStem = stem.toLowerCase(language.getLocale());
		final String lowerCaseComposingText = textField == null ? "" : textField.getComposingText().toLowerCase(language.getLocale());
		if (!lowerCaseStem.equals(lowerCaseComposingText)) {
			Logger.d(LOG_TAG, "Composing text '" + (textField == null ? "" : textField.getComposingText()) + "' does not match the provided stem '" + stem + "'. Recomposing cancelled.");
			return false;
		}

		try {
			digitSequence = language.getDigitSequenceForWord(stem);
		} catch (InvalidLanguageCharactersException ex) {
			return false;
		}

		numberAtPosition = getCurrentSequenceNumber();

		Logger.d(LOG_TAG, "Full-recomposing started for word: " + stem + " (digit sequence: " + digitSequence + ")");

		return true;
	}


	public void skipLetter(boolean left) {
		final String composingText = textField == null ? "" : textField.getComposingText();
		if (position < 0 || position >= composingText.length()) {
			Logger.d(LOG_TAG, "Current cursor position " + position + " is out of bounds for composing text: '" + composingText + "'. Moving failed.");
			return;
		}

		final boolean backward = (left && !LanguageKind.isRTL(language)) || (!left && LanguageKind.isRTL(language));
		if (backward && position > 0) {
			prefix = prefix.isEmpty() ? "" : prefix.substring(0, prefix.length() - 1);
			suffix = new Text(language, composingText.charAt(position)).toTextCase(textCase) + suffix;
			position--;
		} else if (!backward && position < composingText.length() - 1) {
			suffix = suffix.isEmpty() ? "" : suffix.substring(1);
			prefix += new Text(language, composingText.charAt(position)).toTextCase(textCase);
			position++;
		} else {
			return;
		}

		final int textCaseAtNewPosition = new Text(language, composingText.charAt(position)).isUpperCase() ? CASE_UPPER : CASE_LOWER;

		wordAfterBackspace = null;
		numberAtPosition = getCurrentSequenceNumber();
		suggestions.clear();
		loadLettersForPosition(composingText, textCaseAtNewPosition);
		super.loadSuggestions("");
	}


	@Override
	public boolean containsGeneratedSuggestions() {
		return true;
	}


	@NonNull
	@Override
	public String getRecomposingSuffix() {
		return suffix;
	}


	@Override
	public int getRecommendedSuggestionIdx() {
		return suggestionRecommendation;
	}


	@Override
	public String getWordStem() {
		final String originalWord = textField == null ? "" : textField.getComposingText();
		return digitSequence.length() == 1 && prefix.isEmpty() && suggestions.isEmpty() ? originalWord : prefix;
	}


	@Override
	public void onAcceptSuggestion(@NonNull String w, boolean p) {
		finish();
	}


	@Override
	public boolean onReplaceSuggestion(@NonNull String nextLetter) {
		if (position >= digitSequence.length()) {
			return false;
		}

		prefix += nextLetter;
		position++;
		numberAtPosition = getCurrentSequenceNumber();
		loadSuggestions("");
		return true;
	}


	@Override
	public boolean shouldReplacePreviousSuggestion(@Nullable String w) {
		return position + 1 < digitSequence.length();
	}


	@Override
	public boolean shouldSelectNextSuggestion() {
		return previousNumberAtPosition == numberAtPosition;
	}


	@Override
	protected String adjustSuggestionTextCase(String lastChar, int newTextCase) {
		return new Text(language, lastChar).toTextCase(newTextCase);
	}


	private void finish() {
		if (!suggestions.isEmpty()) {
			reset();
		}
	}


	private int getCurrentSequenceNumber() {
		if (position < digitSequence.length()) {
			return digitSequence.charAt(position) - '0';
		}
		return END_OF_WORD;
	}


	private void loadLettersForPosition(@NonNull String originalWord, int withTextCase) {
		suggestionRecommendation = 0;

		final Text letterAtPos = position >= 0 && position < originalWord.length() ? new Text(language, originalWord.charAt(position)) : new Text(null);
		final ArrayList<String> keyChars = language.getKeyCharacters(numberAtPosition);
		keyChars.add(language.getKeyNumeral(numberAtPosition));


		if (withTextCase != CASE_UNDEFINED) {
			textCase = withTextCase;
		} else if (letterAtPos.isAlphabetic()) {
			textCase = letterAtPos.getTextCase();
		}
		final boolean isUpperCase = textCase == CASE_UPPER;

		for (int i = 0; i < keyChars.size(); i++) {
			String suggestionChar = isUpperCase ? keyChars.get(i).toUpperCase(language.getLocale()) : keyChars.get(i);
			suggestions.add(suggestionChar);
			if (suggestionChar.toLowerCase(language.getLocale()).equals(letterAtPos.toLowerCase())) {
				suggestionRecommendation = i;
			}
		}
	}


	private void loadSuffixForPosition(@NonNull String originalWord) {
		suffix = position >= 0 && position < originalWord.length() - 1 ? originalWord.substring(position + 1) : "";
	}


	private void updateDigitSequence() {
		if (numberAtPosition == 0) {
			return;
		}

		previousNumberAtPosition = getCurrentSequenceNumber();

		StringBuilder newSequence = new StringBuilder();
		for (int i = 0; i < digitSequence.length(); i++) {
			if (i == position) {
				newSequence.append(numberAtPosition);
			} else {
				newSequence.append(digitSequence.charAt(i));
			}
		}

		digitSequence = newSequence.toString();
	}


	@NonNull
	@Override
	public String toString() {
		final String originalWord = textField != null ? textField.getComposingText() : "";
		return originalWord + " => " + prefix + "?" + suffix;
	}
}
