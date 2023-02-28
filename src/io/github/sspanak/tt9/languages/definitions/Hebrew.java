package io.github.sspanak.tt9.languages.definitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.Characters;

public class Hebrew extends Language {
	public Hebrew() {
		name = "עברית";
		locale = new Locale("iw","IL");
		dictionaryFile = "he-utf8.txt";
		icon = R.drawable.ime_lang_he;
		abcLowerCaseIcon = R.drawable.ime_lang_he_lower;
    abcUpperCaseIcon = R.drawable.ime_lang_he_upper;

		isPunctuationPartOfWords = true;

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
