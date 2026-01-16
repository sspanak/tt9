package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.chars.Characters;

public class ModeRecomposing extends InputMode {
	private final static String LOG_TAG = ModeRecomposing.class.getSimpleName();
	private final static int END_OF_WORD = -1;

	private int numberAtPosition = END_OF_WORD;
	private int previousNumberAtPosition = END_OF_WORD;
	private int position = 0;
	@Nullable private String originalWord = null;
	@NonNull private String recomposedWord = "";
	@NonNull private String recomposedWordSuffix = "";
	private int suggestionRecommendation = 0;

	@Nullable private Runnable onFinishListener = null;


	@Override public int getId() { return MODE_RECOMPOSING; }


	protected ModeRecomposing(@NonNull SettingsStore settings, @NonNull Language lang, @Nullable InputType inputType) {
		super(settings, inputType);
		setLanguage(lang);
	}


	@Override
	public void reset() {
		super.reset();
		digitSequence = "";
		originalWord = null;
		numberAtPosition = END_OF_WORD;
		previousNumberAtPosition = END_OF_WORD;
		position = 0;
		recomposedWord = "";
		recomposedWordSuffix = "";
		suggestionRecommendation = 0;
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


	public void setOnFinishListener(@Nullable Runnable listener) {
		onFinishListener = listener;
	}


	@Override
	public boolean setWordStem(String stem, boolean scrolling) {
		if (!scrolling) {
			reset();
		}

		if (scrolling || stem == null || stem.length() < SettingsStore.ADD_WORD_MIN_LENGTH) {
			return false;
		}

		try {
			digitSequence = language.getDigitSequenceForWord(stem);
		} catch (InvalidLanguageCharactersException ex) {
			return false;
		}

		numberAtPosition = getCurrentSequenceNumber();
		originalWord = stem;

		return true;
	}


	@Override
	public boolean onBackspace() {
		finish();
		return false;
	}


	@Override
	public void onCursorMove(@NonNull String w) {
		finish();
	}


	@Override
	public boolean onNumber(int number, boolean hold, int repeat, @NonNull String[] surroundingChars) {
		numberAtPosition = number;
		updateDigitSequence();
		return true;
	}


	@Override
	public void loadSuggestions(String w) {
		if (originalWord == null || originalWord.isEmpty() || digitSequence.isEmpty()) {
			Logger.d(LOG_TAG, "Cannot recompose an empty word. Use setWordStem() first.");
			suggestions.clear();
		} else if (numberAtPosition == END_OF_WORD) {
			reset();
		} else if (numberAtPosition == 0) { // Space aborts the process
			autoAcceptTimeout = 0;
			position = originalWord.length();
			suggestions.clear();
			loadCurrentWord(originalWord + Characters.getSpace(language));
		} else {
			suggestions.clear();
			loadLettersForPosition(originalWord);
		}

		super.loadSuggestions(w);
	}


	@Override
	public boolean containsGeneratedSuggestions() {
		return true;
	}


	@NonNull
	@Override
	public String getRecomposingSuffix() {
		return recomposedWordSuffix;
	}


	@Override
	public int getRecommendedSuggestionIdx() {
		return suggestionRecommendation;
	}


	@Override
	public String getWordStem() {
		return recomposedWord;
	}


	@Override
	public void onAcceptSuggestion(@NonNull String word, boolean preserveWordList) {
		finish();
	}


	@Override
	public boolean onReplaceSuggestion(@NonNull String rawWord) {
		if (position >= digitSequence.length()) {
			return false;
		}

		recomposedWord = rawWord;
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
	protected String adjustSuggestionTextCase(String word, int newTextCase) {
		if (!language.hasUpperCase()) {
			return word;
		}

		String lastChar = word.substring(word.length() - 1);
		lastChar = newTextCase == CASE_UPPER ? lastChar.toUpperCase(language.getLocale()) : lastChar.toLowerCase(language.getLocale());

		return word.substring(0, word.length() - 1) + lastChar;
	}


	private void finish() {
		if (suggestions.isEmpty()) {
			return;
		}

		reset();
		if (onFinishListener != null) {
			onFinishListener.run();
		}
	}


	private int getCurrentSequenceNumber() {
		if (position < digitSequence.length()) {
			return digitSequence.charAt(position) - '0';
		}
		return END_OF_WORD;
	}


	private void loadCurrentWord(@NonNull String originalWord) {
		final int joinPosition = recomposedWord.length();
		recomposedWordSuffix = joinPosition < originalWord.length() ? originalWord.substring(joinPosition) : "";
		suggestionRecommendation = 0;

		suggestions.add(recomposedWord + recomposedWordSuffix);
	}


	private void loadLettersForPosition(@NonNull String originalWord) {
		recomposedWordSuffix = position >= 0 && position < originalWord.length() - 1 ? originalWord.substring(position + 1) : "";
		suggestionRecommendation = 0;

		final String letterAtPos = position >= 0 && position < originalWord.length() ? String.valueOf(originalWord.charAt(position)) : "";
		final ArrayList<String> keyChars = language.getKeyCharacters(numberAtPosition);
		keyChars.add(language.getKeyNumeral(numberAtPosition));

		for (int i = 0; i < keyChars.size(); i++) {
			suggestions.add(recomposedWord + keyChars.get(i));
			if (keyChars.get(i).equals(letterAtPos)) {
				suggestionRecommendation = i;
			}
		}
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
		return originalWord + " => " + recomposedWord + "?" + recomposedWordSuffix;
	}
}
