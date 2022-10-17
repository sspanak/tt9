package io.github.sspanak.tt9.languages;

import java.util.ArrayList;
import java.util.Locale;


public class Language {
	protected int id;
	protected String name;
	protected Locale locale;
	protected boolean isPunctuationPartOfWords; // see the getter for more info
	protected int icon;
	protected String dictionaryFile;
	protected int abcLowerCaseIcon;
	protected int abcUpperCaseIcon;
	protected ArrayList<ArrayList<String>> characterMap = new ArrayList<>();

	final public int getId() {
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

	final public String getDictionaryFile() {
		return dictionaryFile;
	}

	final public int getAbcIcon(boolean lowerCase) {
		return lowerCase ? abcLowerCaseIcon : abcUpperCaseIcon;
	}

	public ArrayList<String> getKeyCharacters(int key) {
		if (key < 0 || key >= characterMap.size()) {
			return new ArrayList<>();
		}

		ArrayList<String> chars = new ArrayList<>(characterMap.get(key));
		if (chars.size() > 0) {
			chars.add(String.valueOf(key));
		}

		return chars;
	}

	public String getDigitSequenceForWord(String word) throws Exception {
		StringBuilder sequence = new StringBuilder();
		String lowerCaseWord = word.toLowerCase(locale);

		for (int i = 0; i < lowerCaseWord.length(); i++) {
			for (int key = 0; key <= 9; key++) {
				if (getKeyCharacters(key).contains(Character.toString(lowerCaseWord.charAt(i)))) {
					sequence.append(key);
				}
			}
		}

		if (word.length() != sequence.length()) {
			throw new Exception(
				"Failed generating digit sequence for word: '" + word + "'. Some characters are not supported in language: " + name
			);
		}

		return sequence.toString();
	}
}
