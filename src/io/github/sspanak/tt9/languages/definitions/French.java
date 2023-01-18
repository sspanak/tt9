package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.R;

public class French extends English {
	public French() {
		super();

		id = 4;
		name = "Français";
		locale = Locale.FRENCH;
		dictionaryFile = "fr-utf8.csv";
		icon = R.drawable.ime_lang_fr;

		isPunctuationPartOfWords = false;

		characterMap.get(2).addAll(Arrays.asList("à", "â", "æ", "ç"));
		characterMap.get(3).addAll(Arrays.asList("é", "è", "ê", "ë"));
		characterMap.get(4).addAll(Arrays.asList("î", "ï"));
		characterMap.get(6).addAll(Arrays.asList("ô", "œ"));
		characterMap.get(8).addAll(Arrays.asList("ù", "û", "ü"));
		characterMap.get(9).add("ÿ");
	}
}
