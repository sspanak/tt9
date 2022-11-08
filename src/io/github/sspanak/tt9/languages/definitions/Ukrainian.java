package io.github.sspanak.tt9.languages.definitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.Punctuation;

public class Ukrainian extends Language {
	public Ukrainian() {
		id = 6;
		name = "Українська";
		locale = new Locale("uk","UA");
		dictionaryFile = "uk-utf8.txt";
		icon = R.drawable.ime_lang_uk;
		abcLowerCaseIcon = R.drawable.ime_lang_cyrillic_lower;
		abcUpperCaseIcon = R.drawable.ime_lang_cyrillic_upper;

		isPunctuationPartOfWords = true;

		characterMap = new ArrayList<>(Arrays.asList(
			Punctuation.Secondary, // 0
			Punctuation.Main, // 1
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
