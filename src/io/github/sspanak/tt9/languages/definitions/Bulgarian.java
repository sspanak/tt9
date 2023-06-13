package io.github.sspanak.tt9.languages.definitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.Characters;

public class Bulgarian extends Language {
	public Bulgarian() {
		locale = new Locale("bg","BG");
		dictionaryFile = "bg-utf8.csv";

		characterMap = new ArrayList<>(Arrays.asList(
			Characters.Special, // 0
			Characters.Sentence, // 1
			new ArrayList<>(Arrays.asList("а", "б", "в", "г")), // 2
			new ArrayList<>(Arrays.asList("д", "е", "ж", "з")), // 3
			new ArrayList<>(Arrays.asList("и", "й", "к", "л", "ѝ")), // 4
			new ArrayList<>(Arrays.asList("м", "н", "о", "п")), // 5
			new ArrayList<>(Arrays.asList("р", "с", "т", "у")), // 6
			new ArrayList<>(Arrays.asList("ф", "х", "ц", "ч")), // 7
			new ArrayList<>(Arrays.asList("ш", "щ", "ъ")), // 8
			new ArrayList<>(Arrays.asList("ь", "ю", "я")) // 9
		));
	}
}
