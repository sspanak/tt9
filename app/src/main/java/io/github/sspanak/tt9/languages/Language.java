package io.github.sspanak.tt9.languages;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;

abstract public class Language {
	protected int id;
	protected String abcString;
	protected String code;
	protected String dictionaryFile;
	protected Locale locale = Locale.ROOT;
	protected String name;
	protected boolean hasSpaceBetweenWords = true;
	protected boolean hasUpperCase = true;


	public int getId() {
		return id;
	}

	@NonNull public String getAbcString() {
		return abcString;
	}

	@NonNull public String getCode() {
		return code;
	}

	@NonNull final public String getDictionaryFile() {
		return dictionaryFile;
	}

	@NonNull final public Locale getLocale() {
		return locale;
	}

	/**
	 * Returns the characters that the key would type in ABC or Predictive mode. For example,
	 * the key 2 in English would return A-B-C.
	 * Keys that have special characters assigned, may have more than one group assigned. The specific
	 * group is selected by the characterGroup parameter. By default, group 0 is returned.
	 */
	@NonNull abstract public ArrayList<String> getKeyCharacters(int key, int characterGroup);
	@NonNull public ArrayList<String> getKeyCharacters(int key) {
		return getKeyCharacters(key, 0);
	}

	@NonNull public String getKeyNumber(int key) {
		return String.valueOf(key);
	}

	@NonNull public String getName() {
		return name;
	}

	final public boolean hasSpaceBetweenWords() {
		return hasSpaceBetweenWords;
	}

	final public boolean hasUpperCase() {
		return hasUpperCase;
	}

	@NonNull
	@Override
	final public String toString() {
		return getName();
	}


	/**
	 * Checks whether the given word contains characters outside of the language alphabet.
	 */
	abstract public boolean isValidWord(String word);

	/**
	 * Converts a word to a sequence of digits based on the language's keyboard layout.
	 * For example: "food" -> "3663"
	 */
	@NonNull abstract public String getDigitSequenceForWord(String word) throws InvalidLanguageCharactersException;
}
