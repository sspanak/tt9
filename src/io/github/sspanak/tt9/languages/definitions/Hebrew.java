package io.github.sspanak.tt9.languages.definitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.languages.Characters;
import io.github.sspanak.tt9.languages.Language;

public class Hebrew extends Language {
	public Hebrew() {
		locale = new Locale("iw","IL");
		dictionaryFile = "he-utf8.csv";
		abcString = "אבג";

		hasUpperCase = false;

		characterMap = new ArrayList<>(Arrays.asList(
			Characters.Special, // 0
			Characters.Sentence, // 1
			new ArrayList<>(Arrays.asList("ד", "ה", "ו")), // 2
			new ArrayList<>(Arrays.asList("א", "ב", "ג")), // 3
			new ArrayList<>(Arrays.asList("מ", "ם", "נ", "ן")), // 4
			new ArrayList<>(Arrays.asList("י", "כ", "ך", "ל")), // 5
			new ArrayList<>(Arrays.asList("ז", "ח", "ט")), // 6
			new ArrayList<>(Arrays.asList("ר", "ש", "ת")), // 7
			new ArrayList<>(Arrays.asList("צ", "ץ", "ק")), // 8
			new ArrayList<>(Arrays.asList("ס", "ע", "פ", "ף")) // 9
		));
	}
}
