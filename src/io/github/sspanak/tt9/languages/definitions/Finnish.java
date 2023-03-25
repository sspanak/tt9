package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.R;

public class Finnish extends English {
	public Finnish() {
		super();

		name = "Suomi";
		locale = new Locale("fi","FI");
		dictionaryFile = "fi-utf8.csv";
		icon = R.drawable.ime_lang_fi;

		isPunctuationPartOfWords = true;

		characterMap.get(2).addAll(Arrays.asList("ä", "å"));
		characterMap.get(6).addAll(Arrays.asList("ö"));
	}
}
