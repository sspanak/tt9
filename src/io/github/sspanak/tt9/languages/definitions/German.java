package io.github.sspanak.tt9.languages.definitions;

import java.util.Locale;

public class German extends English {
	public German() {
		super();

		locale = Locale.GERMAN;
		dictionaryFile = "de-utf8.csv";

		characterMap.get(2).add("ä");
		characterMap.get(6).add("ö");
		characterMap.get(7).add("ß");
		characterMap.get(8).add("ü");
	}
}
