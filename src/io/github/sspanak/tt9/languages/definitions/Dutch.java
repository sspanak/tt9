package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.R;

public class Dutch extends English {
	public Dutch() {
		super();

		id = 8;
		name = "Nederlands";
		locale = new Locale("nl","NL");
		isPunctuationPartOfWords = true;
		dictionaryFile = "nl-utf8.txt";
		icon = R.drawable.ime_lang_nl;

		characterMap.get(2).addAll(Arrays.asList("à", "ä", "ç"));
		characterMap.get(3).addAll(Arrays.asList("é", "è", "ê", "ë"));
		characterMap.get(4).addAll(Arrays.asList("î", "ï"));
		characterMap.get(6).add("ö");
		characterMap.get(8).addAll(Arrays.asList("û", "ü"));
	}
}
