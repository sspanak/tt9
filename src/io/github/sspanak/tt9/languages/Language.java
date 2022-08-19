package io.github.sspanak.tt9.languages;

import java.util.ArrayList;
import java.util.Locale;


public class Language {
	protected int id;
	protected String name;
	protected Locale locale;
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

	final public String getDictionaryFile() {
		return dictionaryFile;
	}

	final public int getAbcIcon(boolean lowerCase) {
		return lowerCase ? abcLowerCaseIcon : abcUpperCaseIcon;
	}

	public ArrayList<String> getKeyCharacters(int key) {
		return getKeyCharacters(key, true);
	}

	public ArrayList<String> getKeyCharacters(int key, boolean lowerCase) {
		if (key < 0 || key >= characterMap.size()) {
			return new ArrayList<>();
		}

		ArrayList<String> chars = lowerCase ? new ArrayList<>(characterMap.get(key)) : getUpperCaseChars(key);
		if (chars.size() > 0) {
			chars.add(String.valueOf(key));
		}

		return chars;
	}

	private ArrayList<String> getUpperCaseChars(int mapId) {
		ArrayList<String> uppercaseChars = new ArrayList<>();
		for (String ch : characterMap.get(mapId)) {
			uppercaseChars.add(ch.toUpperCase(locale));
		}

		return uppercaseChars;
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
