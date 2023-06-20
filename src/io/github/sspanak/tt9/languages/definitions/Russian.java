package io.github.sspanak.tt9.languages.definitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.languages.Characters;
import io.github.sspanak.tt9.languages.Language;

public class Russian extends Language {
	public Russian() {
		locale = new Locale("ru","RU");
		dictionaryFile = "ru-utf8.csv";

		characterMap = new ArrayList<>(Arrays.asList(
			Characters.Special, // 0
			Characters.Sentence, // 1
			new ArrayList<>(Arrays.asList("а", "б", "в", "г")), // 2
			new ArrayList<>(Arrays.asList("д", "е", "ё", "ж", "з")), // 3
			new ArrayList<>(Arrays.asList("и", "й", "к", "л")), // 4
			new ArrayList<>(Arrays.asList("м", "н", "о", "п")), // 5
			new ArrayList<>(Arrays.asList("р", "с", "т", "у")), // 6
			new ArrayList<>(Arrays.asList("ф", "х", "ц", "ч")), // 7
			new ArrayList<>(Arrays.asList("ш", "щ", "ъ", "ы")), // 8
			new ArrayList<>(Arrays.asList("ь", "э", "ю", "я")) // 9
		));
	}
}
