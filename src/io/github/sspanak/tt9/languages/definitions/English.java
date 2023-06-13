package io.github.sspanak.tt9.languages.definitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.languages.Characters;
import io.github.sspanak.tt9.languages.Language;

public class English extends Language {
	public English() {
		locale = Locale.ENGLISH;
		dictionaryFile = "en-utf8.csv";

		characterMap = new ArrayList<>(Arrays.asList(
			Characters.Special, // 0
			Characters.Sentence, // 1
			new ArrayList<>(Arrays.asList("a", "b", "c")), // 2
			new ArrayList<>(Arrays.asList("d", "e", "f")), // 3
			new ArrayList<>(Arrays.asList("g", "h", "i")), // 4
			new ArrayList<>(Arrays.asList("j", "k", "l")), // 5
			new ArrayList<>(Arrays.asList("m", "n", "o")), // 6
			new ArrayList<>(Arrays.asList("p", "q", "r", "s")), // 7
			new ArrayList<>(Arrays.asList("t", "u", "v")), // 8
			new ArrayList<>(Arrays.asList("w", "x", "y", "z")) // 9
		));
	}
}
