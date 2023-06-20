package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

public class Norwegian extends English {
	public Norwegian() {
		super();

		locale = new Locale("nb","NO");
		dictionaryFile = "nb-utf8.csv";

		characterMap.get(2).addAll(Arrays.asList("æ", "å"));
		characterMap.get(3).addAll(Arrays.asList("é", "è"));
		characterMap.get(6).addAll(Arrays.asList("ø", "ó", "ò", "ô"));
		characterMap.get(8).add("ü");
	}
}
