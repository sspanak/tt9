package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.R;

public class Norwegian extends English {
	public Norwegian() {
		super();

		id = 10;
		name = "Norsk";
		locale = new Locale("nb","NB");
		dictionaryFile = "nb-utf8.csv";
		icon = R.drawable.ime_lang_nb;

		isPunctuationPartOfWords = true;

		characterMap.get(2).addAll(Arrays.asList("æ", "å"));
		characterMap.get(6).add("ø");
	}
}
