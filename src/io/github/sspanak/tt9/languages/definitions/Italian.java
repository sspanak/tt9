package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

public class Italian extends English {
	public Italian() {
		super();

		locale = Locale.ITALIAN;
		dictionaryFile = "it-utf8.csv";

		characterMap.get(2).add("à");
		characterMap.get(3).addAll(Arrays.asList("é", "è"));
		characterMap.get(4).addAll(Arrays.asList("ì", "í", "î"));
		characterMap.get(6).addAll(Arrays.asList("ò", "ó"));
		characterMap.get(8).addAll(Arrays.asList("ù", "ú"));
	}
}
