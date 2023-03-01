package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.R;

public class Norwegian extends English {
	public Norwegian() {
		super();

		name = "Norsk (Bokmål)";
		locale = new Locale("nb","NO");
		dictionaryFile = "nb-utf8.csv";
		icon = R.drawable.ime_lang_nb;

		isPunctuationPartOfWords = true;

		characterMap.get(2).addAll(Arrays.asList("æ", "å", "ç"));
		characterMap.get(3).addAll(Arrays.asList("é", "è"));
		characterMap.get(6).addAll(Arrays.asList("ø", "ó", "ò", "ô"));
		characterMap.get(8).add("ü");
	}
}
