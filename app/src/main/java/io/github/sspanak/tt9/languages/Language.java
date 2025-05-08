package io.github.sspanak.tt9.languages;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.util.chars.Characters;

abstract public class Language {
	protected int id;
	protected String abcString;
	protected String code;
	protected String currency;
	protected String dictionaryFile;
	protected boolean hasABC = true;
	private Boolean hasLettersOnAllKeys = null;
	protected boolean hasSpaceBetweenWords = true;
	protected boolean hasUpperCase = true;
	protected boolean hasTranscriptionsEmbedded = false;
	protected String iconABC = "";
	protected String iconT9 = "";
	protected boolean isTranscribed = false;
	protected Locale locale = Locale.ROOT;
	protected String name;


	public int getId() {
		return id;
	}

	@NonNull public String getAbcString() {
		return abcString;
	}

	@NonNull public String getCode() {
		return code;
	}

	@NonNull public String getCurrency() {
		return currency;
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
	 */
	@NonNull abstract public ArrayList<String> getKeyCharacters(int key);

	@NonNull public String getKeyNumeral(int key) {
		return String.valueOf(key);
	}

	@NonNull public String getName() {
		return name;
	}

	final public boolean hasABC() {
		return hasABC;
	}

	final public boolean hasLettersOnAllKeys() {
		if (hasLettersOnAllKeys != null) {
			return hasLettersOnAllKeys;
		}

		boolean hasCharsOn0 = false;
		for (String ch : getKeyCharacters(0)) {
			if (Character.isAlphabetic(ch.charAt(0)) && !Characters.isOm(ch.charAt(0))) {
				hasCharsOn0 = true;
				break;
			}
		}

		boolean hasCharsOn1 = false;
		for (String ch : getKeyCharacters(1)) {
			if (Character.isAlphabetic(ch.charAt(0)) && !Characters.isOm(ch.charAt(0))) {
				hasCharsOn1 = true;
				break;
			}
		}

		return hasLettersOnAllKeys = hasCharsOn0 && hasCharsOn1;
	}

	final public boolean hasSpaceBetweenWords() {
		return hasSpaceBetweenWords;
	}

	final public boolean hasUpperCase() {
		return hasUpperCase;
	}

	final public boolean hasTranscriptionsEmbedded() {
		return hasTranscriptionsEmbedded;
	}

	final public String getIconABC() {
		return iconABC;
	}

	final public String getIconT9() {
		return iconT9;
	}

	final public boolean isTranscribed() {
		return isTranscribed;
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
