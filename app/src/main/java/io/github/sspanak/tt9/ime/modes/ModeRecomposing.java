package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.chars.Characters;

public class ModeRecomposing extends InputMode {
	private final static String LOG_TAG = ModeRecomposing.class.getSimpleName();
	private final static int END_OF_WORD = -1;

	private int currentNumber = END_OF_WORD;
	private int position = 0;
	@Nullable private String originalWord = null;
	@NonNull private String recomposedWord = "";


	@Override public int getId() { return MODE_RECOMPOSING; }


	protected ModeRecomposing(@NonNull SettingsStore settings, @NonNull Language lang, @Nullable InputType inputType) {
		super(settings, inputType);
		setLanguage(lang);
	}


	@Override
	public void reset() {
		super.reset();
		currentNumber = END_OF_WORD;
		digitSequence = "";
		originalWord = null;
		position = 0;
		recomposedWord = "";
	}


	@Override
	protected boolean setLanguage(@Nullable Language newLanguage) {
		reset();

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

	@Override
	public boolean onNumber(int number, boolean hold, int repeat, @NonNull String[] surroundingChars) {
		currentNumber = number;
		updateDigitSequence();
		return true;
	}


	@Override
	public boolean onBackspace() {
		if (position > 0) {
			position--;
		}
		return true;
	}


	@Override
	public boolean setWordStem(String stem, boolean scrolling) {
		reset();

		if (scrolling || stem == null || stem.length() < SettingsStore.ADD_WORD_MIN_LENGTH) {
			return false;
		}

		try {
			digitSequence = language.getDigitSequenceForWord(stem);
		} catch (InvalidLanguageCharactersException ex) {
			return false;
		}

		currentNumber = getCurrentSequenceNumber();
		originalWord = stem;

		return true;
	}


	@Override
	public void loadSuggestions(String w) {
		suggestions.clear();

		if (originalWord == null || originalWord.isEmpty() || digitSequence.isEmpty()) {
			Logger.d(LOG_TAG, "No initial word, cannot recompose. position=" + position + " digitSequence='" + digitSequence);
		} else if (currentNumber == END_OF_WORD) {
			reset();
		} else if (currentNumber == 0) { // Space aborts the process
			reset();
			suggestions.add(Characters.getSpace(language));
		} else {
			for (String ch : language.getKeyCharacters(currentNumber)) {
				suggestions.add(recomposedWord + ch);
			}
		}

		super.loadSuggestions(w);
	}


	private int getCurrentSequenceNumber() {
		if (position < digitSequence.length()) {
			return digitSequence.charAt(position) - '0';
		}
		return END_OF_WORD;
	}


	private void updateDigitSequence() {
		if (currentNumber == 0) {
			return;
		}

		StringBuilder newSequence = new StringBuilder();
		for (int i = 0; i < digitSequence.length(); i++) {
			if (i == position) {
				newSequence.append(currentNumber);
			} else {
				newSequence.append(digitSequence.charAt(i));
			}
		}

		digitSequence = newSequence.toString();
	}

	@Override
	public boolean nextTextCase(@Nullable String currentWord, int displayTextCase) {
		return super.nextTextCase(currentWord, displayTextCase);
	}

	@Override
	public boolean containsGeneratedSuggestions() {
		return true;
	}


	@Override
	public boolean shouldSelectNextSuggestion() {
		return getCurrentSequenceNumber() == currentNumber;
	}


	@Override
	public boolean shouldReplacePreviousSuggestion(@Nullable String w) {
		return !shouldAcceptPreviousSuggestion(null);
	}


	@Override
	public boolean onReplaceSuggestion(@NonNull String rawWord) {
		if (position >= digitSequence.length()) {
			return false;
		}

		// @todo: fix the initial failure
		// @todo: textField.recompose() does not erase the text after the cursor. Fix that.
		// @todo: also pre-select the appropriate character
		// @todo: print the end of the original word after the suggestions
		// @todo: moving the text cursor must abort the process

		recomposedWord = rawWord;
		position++;
		currentNumber = getCurrentSequenceNumber();
		loadSuggestions("");
		return true;
	}


	@Override
	public boolean shouldAcceptPreviousSuggestion(String w) {
		return currentNumber <= 0;
	}


	@Override
	protected String adjustSuggestionTextCase(String word, int newTextCase) {
		if (!language.hasUpperCase()) {
			return word;
		}

		String lastChar = word.substring(word.length() - 1);
		lastChar = newTextCase == CASE_UPPER ? lastChar.toUpperCase(language.getLocale()) : lastChar.toLowerCase(language.getLocale());

		return word.substring(0, word.length() - 1) + lastChar;
	}


	@NonNull
	@Override
	public String toString() {
		return "RECOMPOSING: '" + originalWord + "'";
	}
}
