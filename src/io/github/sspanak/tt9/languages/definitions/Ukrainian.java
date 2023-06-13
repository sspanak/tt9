package io.github.sspanak.tt9.languages.definitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.languages.Characters;
import io.github.sspanak.tt9.languages.Language;

public class Ukrainian extends Language {
	public Ukrainian() {
		locale = new Locale("uk","UA");
		dictionaryFile = "uk-utf8.csv";

		characterMap = new ArrayList<>(Arrays.asList(
			Characters.Special, // 0
			Characters.Sentence, // 1
			new ArrayList<>(Arrays.asList("а", "б", "в", "г", "ґ")), // 2
			new ArrayList<>(Arrays.asList("д", "е", "є", "ж", "з")), // 3
			new ArrayList<>(Arrays.asList("и", "і", "ї", "й", "к", "л")), // 4
			new ArrayList<>(Arrays.asList("м", "н", "о", "п")), // 5
			new ArrayList<>(Arrays.asList("р", "с", "т", "у")), // 6
			new ArrayList<>(Arrays.asList("ф", "х", "ц", "ч")), // 7
			new ArrayList<>(Arrays.asList("ш", "щ")), // 8
			new ArrayList<>(Arrays.asList("ь", "ю", "я")) // 9
		));
	}
}
