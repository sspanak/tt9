package io.github.sspanak.tt9.languages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.R;

public class English extends Language {
	public English() {
		id = 1;
		name = "English";
		locale = Locale.ENGLISH;
		dictionaryFile = "en-utf8.txt";
		icon = R.drawable.ime_lang_en;
		abcLowerCaseIcon = R.drawable.ime_lang_latin_lower;
		abcUpperCaseIcon = R.drawable.ime_lang_latin_upper;

		characterMap = new ArrayList<>(Arrays.asList(
			Punctuation.Secondary, // 0
			Punctuation.Main, // 1
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
