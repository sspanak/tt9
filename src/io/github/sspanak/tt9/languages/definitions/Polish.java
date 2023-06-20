package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

public class Polish extends English {
	public Polish() {
		super();

		locale = new Locale("pl","PL");
		dictionaryFile = "pl-utf8.csv";

		characterMap.get(2).addAll(Arrays.asList("ą", "ć"));
		characterMap.get(3).add("ę");
		characterMap.get(5).add("ł");
		characterMap.get(6).addAll(Arrays.asList("ó", "ń"));
		characterMap.get(7).add("ś");
		characterMap.get(9).addAll(Arrays.asList("ź", "ż"));
	}
}
