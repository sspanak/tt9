package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

public class Swedish extends English {
	public Swedish() {
		super();

		locale = new Locale("sv","SE");
		dictionaryFile = "sv-utf8.csv";

		characterMap.get(2).addAll(Arrays.asList("å", "ä"));
		characterMap.get(3).add("é");
		characterMap.get(6).add("ö");
	}
}
