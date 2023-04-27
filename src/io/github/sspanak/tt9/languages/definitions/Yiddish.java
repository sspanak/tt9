package io.github.sspanak.tt9.languages.definitions;

import java.util.Locale;

public class Yiddish extends Hebrew {
	public Yiddish() {
		super();

		locale = new Locale("yi","YI");
		dictionaryFile = "yiddish-utf8.csv";
		hasUpperCase = false;
		isPunctuationPartOfWords = true;


	}
}
