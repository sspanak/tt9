package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

public class Finnish extends English {
	public Finnish() {
		super();

		locale = new Locale("fi","FI");
		dictionaryFile = "fi-utf8.csv";

		characterMap.get(2).addAll(Arrays.asList("ä", "å"));
		characterMap.get(6).add("ö");
	}
}
