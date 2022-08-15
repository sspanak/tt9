package io.github.sspanak.tt9.languages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;


public class Language {
	protected int id = 0;
	protected String name = "";
	protected Locale locale;
	protected int icon = 0;
	protected int abcLowerCaseIcon = 0;
	protected int abcUpperCaseIcon = 0;
	protected ArrayList<ArrayList<String>> characterMap = new ArrayList<>(Arrays.asList(
		new ArrayList<>(),	// 2
		new ArrayList<>(),	// 3
		new ArrayList<>(),	// 4
		new ArrayList<>(),	// 5
		new ArrayList<>(),	// 6
		new ArrayList<>(),	// 7
		new ArrayList<>(),	// 8
		new ArrayList<>()		// 9
	));


	public int getId() {
		return id;
	}

	public Locale getLocale() {
		return locale;
	}

	public String getName() {
		return name;
	}

	public int getIcon() {
		return icon;
	}

	public int getAbcIcon(boolean lowerCase) {
		return lowerCase ? abcLowerCaseIcon : abcUpperCaseIcon;
	}


	private ArrayList<String> getUpperCaseChars(int mapId) {
		ArrayList<String> uppercaseChars = new ArrayList<>();
		for (String ch : characterMap.get(mapId)) {
			uppercaseChars.add(ch.toUpperCase(locale));
		}

		return uppercaseChars;
	}


	public ArrayList<String> getKeyLettersOnly(int key, boolean lowerCase) {
		if (key < 2 || key > 9) {
			return new ArrayList<>();
		}

		return lowerCase ? new ArrayList<>(characterMap.get(key - 2)) : getUpperCaseChars(key - 2);
	}


	public ArrayList<String>getKeyCharacters(int key, boolean lowerCase) {
		ArrayList<String> chars;

		if (key == 0) {
			return Punctuation.getPunctuation();
		} else if (key == 1) {
			return Punctuation.getSecondaryPunctuation();
		} else {
			chars = getKeyLettersOnly(key, lowerCase);
			if (chars.size() > 0) {
				chars.add(String.valueOf(key));
			}
		}

		return chars;
	}
}
