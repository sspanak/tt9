package io.github.sspanak.tt9.languages.definitions;

import java.util.Locale;

import io.github.sspanak.tt9.R;

public class German extends English {
	public German() {
		super();

		id = 3;
		name = "Deutsch";
		locale = Locale.GERMAN;
		dictionaryFile = "de-utf8.txt";
		icon = R.drawable.ime_lang_de;

		isPunctuationPartOfWords = false;

		characterMap.get(2).add("ä");
		characterMap.get(6).add("ö");
		characterMap.get(7).add("ß");
		characterMap.get(8).add("ü");
	}
}
