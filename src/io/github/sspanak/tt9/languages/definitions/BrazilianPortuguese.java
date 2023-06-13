package io.github.sspanak.tt9.languages.definitions;

import java.util.Arrays;
import java.util.Locale;

public class BrazilianPortuguese extends English {
	public BrazilianPortuguese() {
		super();

		name = "Português brasileiro";
		locale = new Locale("pt","BR");
		dictionaryFile = "pt-BR-utf8.csv";

		characterMap.get(2).addAll(Arrays.asList("ç", "á", "â", "ã", "à"));
		characterMap.get(3).addAll(Arrays.asList("é", "ê", "è"));
		characterMap.get(4).add("í");
		characterMap.get(6).addAll(Arrays.asList("ó", "ô", "õ"));
		characterMap.get(8).add("ú");
	}
}
