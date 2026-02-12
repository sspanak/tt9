package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.modes.helpers.AutoSpace;
import io.github.sspanak.tt9.ime.modes.helpers.AutoTextCase;
import io.github.sspanak.tt9.ime.modes.helpers.Sequences;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.chars.Characters;

class ModeABC extends InputMode {
	private final ArrayList<ArrayList<String>> KEY_CHARACTERS = new ArrayList<>();

	private boolean shouldSelectNextLetter = false;

	// text analysis
	@NonNull private final AutoSpace autoSpace;
	@NonNull private final AutoTextCase autoTextCase;
	@Nullable private final InputType inputType;
	private final int textFieldTextCase;

	@Override public int getId() { return MODE_ABC; }


	protected ModeABC(@NonNull SettingsStore settings, @NonNull Language lang, @Nullable InputType inputType) {
		super(settings, inputType);
		autoSpace = new AutoSpace(settings);
		autoTextCase = new AutoTextCase(settings, new Sequences(), inputType);
		this.inputType = inputType;
		textFieldTextCase = inputType == null ? CASE_UNDEFINED : inputType.determineTextCase();

		setLanguage(lang);
		defaultTextCase();
	}


	@Override
	public boolean onBackspace() {
		if (!suggestions.isEmpty()) {
			reset();
		}

		return false;
	}


	@Override
	public boolean onNumber(int number, boolean hold, int repeat, @NonNull String[] s) {
		return onNumber(number, hold, repeat);
	}


	private boolean onNumber(int number, boolean hold, int repeat) {
		if (hold) {
			reset();
			autoAcceptTimeout = 0;
			digitSequence = String.valueOf(number);
			shouldSelectNextLetter = false;
			suggestions.add(language.getKeyNumeral(number));
		} else if (repeat > 0 && !suggestions.isEmpty()) {
			autoAcceptTimeout = settings.getAutoAcceptTimeoutAbc();
			shouldSelectNextLetter = true;
		} else {
			reset();
			autoAcceptTimeout = settings.getAutoAcceptTimeoutAbc();
			digitSequence = String.valueOf(number);
			shouldSelectNextLetter = false;
			suggestions.addAll(KEY_CHARACTERS.size() > number ? KEY_CHARACTERS.get(number) : settings.getOrderedKeyChars(language, number));
			suggestions.add(language.getKeyNumeral(number));
		}

		return true;
	}


	/******** TEXT CASE ********/
	@Override
	protected String adjustSuggestionTextCase(String word, int newTextCase) {
		if (language.hasUpperCase()) {
			return newTextCase == CASE_LOWER ? word.toLowerCase(language.getLocale()) : word.toUpperCase(language.getLocale());
		} else {
			return word;
		}
	}


	@Override
	public void determineNextWordTextCase(@Nullable String beforeCursor, int nextDigit) {
		if (settings.getAutoTextCaseAbc()) {
			textCase = autoTextCase.determineNextLetterTextCase(language, textFieldTextCase, beforeCursor);
		}
	}


	@Override
	public boolean nextTextCase(@Nullable String currentWord, int displayTextCase) {
		if (suggestions.isEmpty()) {
			return super.nextTextCase(currentWord, displayTextCase);
		}

		for (int newTextCase : allowedTextCases) {
			if (newTextCase != textCase && newTextCase != InputMode.CASE_CAPITALIZE) {
				textCase = newTextCase;
				return true;
			}
		}

		return false;
	}


	@Override
	public void skipNextTextCaseDetection() {
		autoTextCase.skipNext();
	}


	/******** AUTO-SPACE ********/
	@Override
	public boolean shouldAddTrailingSpace(@NonNull String previousChars, @NonNull String nextChars, boolean isWordAcceptedManually, int nextKey) {
		return autoSpace.shouldAddTrailingSpace(inputType, this, previousChars, nextChars, isWordAcceptedManually, nextKey);
	}


	@Override
	public boolean shouldAddPrecedingSpace(@NonNull String previousChars) {
		return autoSpace.shouldAddBeforePunctuation(inputType, previousChars);
	}


	@Override
	public boolean shouldDeletePrecedingSpace(@NonNull String previousChars) {
		return autoSpace.shouldDeletePrecedingSpace(inputType, previousChars);
	}


	/******** GENERAL ********/

	private void refreshSuggestions() {
		if (digitSequence.isEmpty()) {
			suggestions.clear();
		} else {
			onNumber(digitSequence.charAt(0) - '0', false, 0);
		}
	}


	@Override
	public boolean setLanguage(@Nullable Language newLanguage) {
		if (newLanguage != null && !newLanguage.hasABC()) {
			return false;
		}

		super.setLanguage(newLanguage);

		autoSpace.setLanguage(newLanguage);

		allowedTextCases.clear();
		allowedTextCases.add(CASE_LOWER);
		if (language.hasUpperCase()) {
			if (settings.getAutoTextCaseAbc()) {
				allowedTextCases.add(CASE_CAPITALIZE);
			}
			allowedTextCases.add(CASE_UPPER);
		}

		KEY_CHARACTERS.clear();
		if (isEmailMode) {
			// Asian punctuation can not be used in email addresses, so we need to use the English locale.
			Language lang = LanguageKind.isCJK(language) ? LanguageCollection.getByLocale("en") : language;
			KEY_CHARACTERS.add(Characters.orderByList(Characters.Email.get(0), settings.getOrderedKeyChars(lang, 0), true));
			KEY_CHARACTERS.add(Characters.orderByList(Characters.Email.get(1), settings.getOrderedKeyChars(lang, 1), true));
		}

		refreshSuggestions();
		shouldSelectNextLetter = true; // do not accept any previous suggestions after loading the new ones

		return true;
	}


	@Override
	public void setSequence(@NonNull String sequence) {
		super.setSequence(sequence);
		refreshSuggestions();
		shouldSelectNextLetter = true;
	}


	@Override public void onAcceptSuggestion(@NonNull String w) {
		reset();
	}


	@Override
	public boolean shouldAcceptPreviousSuggestion(String word) {
		return
			!shouldSelectNextLetter
			&& word != null && !word.isEmpty()
			&& !Characters.PLACEHOLDER.equals(word);
	}


	@Override
	public boolean shouldSelectNextSuggestion() {
		return shouldSelectNextLetter;
	}


	@Override
	public void reset() {
		super.reset();
		digitSequence = "";
		shouldSelectNextLetter = false;
	}


	@NonNull
	@Override
	public String toString() {
		String modeString = language.getAbcString();

		// There are many languages written using the same alphabet, so if the user has
		// enabled multiple ones, make it clear which one is it, by appending the unique
		// country or language code to "ABC" or "АБВ".
		if (LanguageKind.isArabicBased(language) || LanguageKind.isCyrillic(language) || LanguageKind.isHebrew(language) || LanguageKind.isLatinBased(language)) {
			modeString += " / " + language.getCode();
		}

		return new Text(language, modeString).toTextCase(textCase);
	}
}
