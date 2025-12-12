package io.github.sspanak.tt9.ime.modes.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.InputConnectionAsync;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NullLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.chars.Characters;

public class AutoSpace {
	private static final Set<Character> PRECEDING_SPACE_CHARS = Set.of('(', '«', '„');
	private static final Set<Character> PRECEDING_SPACE_CHARS_FRENCH = Set.of(';', ':', '!', '?', '»');
	private static final Set<Character> TRAILING_SPACE_CHARS = Set.of(';', '!', '?', ')', '%', '»', '،', Characters.AR_QUESTION_MARK.charAt(0), '“', Characters.GR_QUESTION_MARK.charAt(0));
	private static final Set<Character> TRAILING_SPACE_POST_DIGIT_CHARS = Set.of(':', '.', ',');
	private static final Set<Character> PADDING_SPACE_CHARS = Set.of('-', '/');

	private static final Set<Character> NO_PRECEDING_SPACE_CHARS = Set.of('.', ',', ')', '\'', '“', Characters.AR_QUESTION_MARK.charAt(0), Characters.GR_QUESTION_MARK.charAt(0));
	private static final Set<Character> NO_PRECEDING_SPACE_CHARS_NOT_FRENCH = Set.of(';', ':', '!', '?', '»');

	private Language language;
	@Nullable private final SettingsStore settings;

	private boolean isLanguageFrench;
	private boolean isLanguageWithAlphabet;
	private boolean isLanguageWithSpaceBetweenWords;


	public AutoSpace(@Nullable SettingsStore settingsStore) {
		language = new NullLanguage();
		settings = settingsStore;
		isLanguageWithAlphabet = false;
		isLanguageFrench = false;
		isLanguageWithSpaceBetweenWords = true;
	}


	public AutoSpace setLanguage(Language lang) {
		language = language == null ? new NullLanguage() : lang;
		isLanguageFrench = LanguageKind.isFrench(lang);
		isLanguageWithAlphabet = !language.isTranscribed();
		isLanguageWithSpaceBetweenWords = language.hasSpaceBetweenWords();
		return this;
	}


	/**
	 * Determines whether to automatically add a space at the end of a sentence or after accepting a
	 * suggestion. This allows faster typing, without pressing space. See the helper functions for
	 * the list of rules.
	 */
	public boolean shouldAddTrailingSpace(@Nullable TextField textField, @Nullable InputType inputType, @NonNull InputMode mode, boolean isWordAcceptedManually, int nextKey) {
		if (
			!isLanguageWithSpaceBetweenWords
			|| nextKey == 0
			|| inputType == null
			|| textField == null
			|| isOff()
			|| inputType.isSpecialized()
			|| inputType.isUs()
		) {
			return false;
		}

		final String previousChars = textField.getStringBeforeCursor(10);

		// If the InputConnection timed out, assume we are right after a word and we want a space.
		// It should be the more convenient option.
		if (previousChars.equals(InputConnectionAsync.TIMEOUT_SENTINEL)) {
			return true;
		}

		final Text nextChars = textField.getTextAfterCursor(language, 2);

		return
			!nextChars.startsWithWhitespace()
			&& (
				shouldAddAfterWord(!InputModeKind.isABC(mode), isWordAcceptedManually, new Text(language, previousChars), nextChars, nextKey)
				|| shouldAddAfterPunctuation(previousChars, nextChars, nextKey)
			);
	}


	/**
	 * Determines the special French rules for space before punctuation, as well as some standard ones.
	 * For example, should we transform "word?" to "word ?", or "something(" to "something ("
	 */
	public boolean shouldAddBeforePunctuation(@Nullable InputType inputType, @Nullable TextField textField) {
		if (
			!isLanguageWithSpaceBetweenWords
			|| inputType == null
			|| textField == null
			|| isOff()
			|| inputType.isSpecialized()
			|| inputType.isUs()
		) {
			return false;
		}

		String previousChars = textField.getStringBeforeCursor(2);

		// if we can't figure out what is before, do not assume we are near punctuation to avoid
		// unexpected spaces
		if (previousChars.equals(InputConnectionAsync.TIMEOUT_SENTINEL)) {
			return false;
		}

		char penultimateChar = previousChars.length() < 2 ? 0 : previousChars.charAt(previousChars.length() - 2);
		char previousChar = previousChars.isEmpty() ? 0 : previousChars.charAt(previousChars.length() - 1);

		if ((previousChar == '¡' || previousChar == '¿') && !Character.isWhitespace(penultimateChar) && penultimateChar != 0) {
			return true;
		}

		return
			Character.isAlphabetic(penultimateChar)
			&& (
				PRECEDING_SPACE_CHARS.contains(previousChar)
				|| (isLanguageFrench && PRECEDING_SPACE_CHARS_FRENCH.contains(previousChar))
			);
	}


	private boolean isOff() {
		return
			settings == null
			|| (settings.getInputMode() == InputMode.MODE_PREDICTIVE && !settings.getAutoSpacePredictive())
			|| (settings.getInputMode() == InputMode.MODE_ABC && !settings.getAutoSpaceAbc());
	}


	/**
	 * Determines whether to automatically adding a space after certain punctuation signs makes sense.
	 * The rules are similar to the ones in the standard Android keyboard (with some exceptions,
	 * because we are not using a QWERTY keyboard here).
	 */
	private boolean shouldAddAfterPunctuation(@NonNull String previousChars, @NonNull Text nextChars, int nextKey) {
		char penultimateChar = previousChars.length() < 2 ? 0 : previousChars.charAt(previousChars.length() - 2);
		char previousChar = previousChars.isEmpty() ? 0 : previousChars.charAt(previousChars.length() - 1);

		return
			nextKey != 1
			&& !Text.nextIsPunctuation(nextChars.toString())
			&& !nextChars.startsWithNumber()
			&& (
				TRAILING_SPACE_CHARS.contains(previousChar)
				|| (isLanguageFrench && previousChar == '«')
				|| (penultimateChar == ' ' && PADDING_SPACE_CHARS.contains(previousChar))
				|| (!Character.isDigit(penultimateChar) && TRAILING_SPACE_POST_DIGIT_CHARS.contains(previousChar))
				|| (
					Character.isDigit(penultimateChar) && Characters.isCurrency(language, String.valueOf(previousChar))
				)
			);
	}


	/**
	 * Similar to "shouldAddAfterPunctuation()", but determines whether to add a space after words.
	 */
	private boolean shouldAddAfterWord(boolean isWordInput, boolean isWordAcceptedManually, @NonNull Text previousChars, @NonNull Text nextChars, int nextKey) {
		return
			isWordInput // in ABC and likes, no space is needed between letters, because it becomes more difficult to use the same key several times in a row
			&& isWordAcceptedManually // Do not add space when auto-accepting words, because it feels very confusing when typing.
			&& isLanguageWithAlphabet
			&& nextKey != 1
			&& (nextChars.isEmpty() || nextChars.startsWithNewline())
			&& previousChars.endsWithLetter();
	}


	/**
	 * Determines whether to transform: "word ." to: "word."
	 */
	public boolean shouldDeletePrecedingSpace(@Nullable InputType inputType, @Nullable TextField textField) {
		if (
			!isLanguageWithSpaceBetweenWords
			|| inputType == null
			|| textField == null
			|| isOff()
			|| inputType.isSpecialized()
			|| inputType.isUs()
		) {
			return false;
		}


		String previousChars = textField.getStringBeforeCursor(3);

		// if we can't figure out what is before, better not delete anything
		if (previousChars.equals(InputConnectionAsync.TIMEOUT_SENTINEL)) {
			return false;
		}

		char prePenultimateChar = previousChars.length() < 3 ? 0 : previousChars.charAt(previousChars.length() - 3);
		char penultimateChar = previousChars.length() < 2 ? 0 : previousChars.charAt(previousChars.length() - 2);
		char previousChar = previousChars.isEmpty() ? 0 : previousChars.charAt(previousChars.length() - 1);

		return
			!Character.isWhitespace(prePenultimateChar)
			&& Character.isWhitespace(penultimateChar)
			&& (
				NO_PRECEDING_SPACE_CHARS.contains(previousChar)
				|| (!isLanguageFrench && NO_PRECEDING_SPACE_CHARS_NOT_FRENCH.contains(previousChar))
			);
	}
}
