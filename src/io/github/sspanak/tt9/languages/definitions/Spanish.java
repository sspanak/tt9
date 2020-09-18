package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import io.github.sspanak.tt9.R;

public class Spanish extends English {
	public Spanish() {
		super();

		id = 9;
		name = "Español";
		locale = new Locale("es", "ES");
		dictionaryFile = "es-utf8.txt";
		icon = R.drawable.ime_lang_es;

		isPunctuationPartOfWords = false;

		characterMap.get(1).addAll(Arrays.asList("¡", "¿"));
		characterMap.get(2).addAll(Collections.singletonList("á"));
		characterMap.get(3).addAll(Collections.singletonList("é"));
		characterMap.get(4).addAll(Collections.singletonList("í"));
		characterMap.get(6).clear(); characterMap.get(6).addAll(Arrays.asList("m", "n", "ñ", "o", "ó"));
		characterMap.get(8).addAll(Arrays.asList("ú", "ü"));
	}
}
