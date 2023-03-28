package io.github.sspanak.tt9.languages.definitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import io.github.sspanak.tt9.languages.Characters;

public class Spanish extends English {
	public Spanish() {
		super();

		name = "Español";
		locale = new Locale("es", "ES");
		dictionaryFile = "es-utf8.csv";

		isPunctuationPartOfWords = false;

		characterMap.set(1, new ArrayList<>(Characters.Sentence));
		characterMap.get(1).addAll(Arrays.asList("¡", "¿"));

		characterMap.get(2).addAll(Collections.singletonList("á"));
		characterMap.get(3).addAll(Collections.singletonList("é"));
		characterMap.get(4).addAll(Collections.singletonList("í"));
		characterMap.set(6, new ArrayList<>(Arrays.asList("m", "n", "ñ", "o", "ó")));
		characterMap.get(8).addAll(Arrays.asList("ú", "ü"));
	}
}
