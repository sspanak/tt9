package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.R;

public class Swedish extends English {
	public Swedish() {
		super();

		name = "Svenska";
		locale = new Locale("sv","SE");
		dictionaryFile = "sv-utf8.csv";
		icon = R.drawable.ime_lang_sv;

		isPunctuationPartOfWords = false;

		characterMap.get(2).addAll(Arrays.asList("å", "ä"));
		characterMap.get(3).addAll(Arrays.asList("é"));
		characterMap.get(6).addAll(Arrays.asList("ö"));
	}
}
