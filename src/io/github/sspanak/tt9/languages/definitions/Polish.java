package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

public class Polish extends English {
	public Polish() {
		super();

		locale = Locale.POLISH;
		dictionaryFile = "pl-utf8.txt";

		isPunctuationPartOfWords = true;

		characterMap.get(2).addAll("ą", "ć"));
		characterMap.get(3).add(Arrays.asList("ę");
		characterMap.get(5).add(Arrays.asList("ł");
		characterMap.get(6).addAll(Arrays.asList("ó", "ń"));
		characterMap.get(7).add(Arrays.asList("ś");
		characterMap.get(9).addAll(Arrays.asList("ź", "ż"));
	}
}
