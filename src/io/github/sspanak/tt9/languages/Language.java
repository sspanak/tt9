package io.github.sspanak.tt9.languages;

import java.util.ArrayList;
import java.util.Locale;


public class Language {
	protected int id;
	protected String name;
	protected Locale locale;
	protected int icon;
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
}
