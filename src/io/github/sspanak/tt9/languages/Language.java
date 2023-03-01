package io.github.sspanak.tt9.languages;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class Language {
	private int id;
	protected String name;
	protected Locale locale;
	protected int icon;
	protected String dictionaryFile;
	protected int abcLowerCaseIcon;
	protected int abcUpperCaseIcon;
	protected ArrayList<ArrayList<String>> characterMap = new ArrayList<>();
	private final HashMap<Character, String> reverseCharacterMap = new HashMap<>();

	// settings
	protected boolean isPunctuationPartOfWords; // see the getter for more info

	final public int getId() {
		if (id == 0) {
			id = generateId();
		}

		return id;
	}

	final public Locale getLocale() {
		return locale;
	}

	final public String getName() {
		return name;
	}

	final public int getIcon() {
		return icon;
	}

	final public String getDictionaryFile() {
		return dictionaryFile;
	}

	final public int getAbcIcon(boolean lowerCase) {
		return lowerCase ? abcLowerCaseIcon : abcUpperCaseIcon;
	}

	/**
	 * isPunctuationPartOfWords
	 * This plays a role in Predictive mode only.
	 *
	 * Return "true", if you need to use the 1-key for typing words, such as:
	 * "it's" (English), "a'tje" (Dutch), "п'ят" (Ukrainian).
	 *
	 * Return "false" also:
	 * 		- hide words like the above from the suggestions.
	 *		- 1-key would commit the current word, then display the punctuation list.
	 * 			For example, pressing 1-key after "it" would accept "it" as a separate word,
	 * 			then display only: | , | . | ! | ? | ...
	 *
	 * "false" is recommended when apostrophes or other punctuation are not part of the words,
	 * because it would allow faster typing.
	 */
	final public boolean isPunctuationPartOfWords() { return isPunctuationPartOfWords; }


	final public boolean hasUpperCase() {
		return abcUpperCaseIcon != 0;
	}


	/* ************ utility ************ */

	/**
	 * generateId
	 * Uses the letters of the Locale to generate an ID for the language.
	 * Each letter is converted to uppercase and used as n 5-bit integer. Then the the 5-bits
	 * are packed to form a 10-bit or a 20-bit integer, depending on the Locale.
	 *
	 * Example (2-letter Locale)
	 * 	"en"
	 * 	-> "E" | "N"
	 * 	-> 5 | 448 (shift the 2nd number by 5 bits, so its bits would not overlap with the 1st one)
	 *	-> 543
	 *
	 * Example (4-letter Locale)
	 * 	"bg-BG"
	 * 	-> "B" | "G" | "B" | "G"
	 * 	-> 2 | 224 | 2048 | 229376 (shift each 5-bit number, not overlap with the previous ones)
	 *	-> 231650
	 */
	private int generateId() {
		String idString = (locale.getLanguage() + locale.getCountry()).toUpperCase();
		int idInt = 0;
		for (int i = 0; i < idString.length(); i++) {
			idInt |= ((idString.charAt(i) & 31) << (i * 5));
		}

		return idInt;
	}

	private void generateReverseCharacterMap() {
		reverseCharacterMap.clear();
		for (int digit = 0; digit <= 9; digit++) {
			for (String keyChar : getKeyCharacters(digit)) {
				reverseCharacterMap.put(keyChar.charAt(0), String.valueOf(digit));
			}
		}
	}

	public String capitalize(String word) {
		return word != null ? word.substring(0, 1).toUpperCase(locale) + word.substring(1).toLowerCase(locale) : null;
	}

	public boolean isMixedCaseWord(String word) {
		return word != null
			&& (
				(word.length() == 1 && word.toUpperCase(locale).equals(word))
				|| (!word.toLowerCase(locale).equals(word) && !word.toUpperCase(locale).equals(word))
			);
	}

	public ArrayList<String> getKeyCharacters(int key, boolean includeDigit) {
		if (key < 0 || key >= characterMap.size()) {
			return new ArrayList<>();
		}

		ArrayList<String> chars = new ArrayList<>(characterMap.get(key));
		if (includeDigit && chars.size() > 0) {
			chars.add(String.valueOf(key));
		}

		return chars;
	}

	public ArrayList<String> getKeyCharacters(int key) {
		return getKeyCharacters(key, true);
	}

	public String getDigitSequenceForWord(String word) throws InvalidLanguageCharactersException {
		StringBuilder sequence = new StringBuilder();
		String lowerCaseWord = word.toLowerCase(locale);

		if (reverseCharacterMap.isEmpty()) {
			generateReverseCharacterMap();
		}

		for (int i = 0; i < lowerCaseWord.length(); i++) {
			char letter = lowerCaseWord.charAt(i);
			if (!reverseCharacterMap.containsKey(letter)) {
				throw new InvalidLanguageCharactersException(this, "Failed generating digit sequence for word: '" + word);
			}

			sequence.append(reverseCharacterMap.get(letter));
		}

		return sequence.toString();
	}

	@NonNull
	@Override
	public String toString() {
		return name != null ? name : "";
	}
}
