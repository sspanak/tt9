package io.github.sspanak.tt9.languages.definitions;

import java.util.Locale;

public class Indonesian extends English {
	public Indonesian() {
		super();

		name = "Bahasa Indonesia";
		locale = new Locale("in", "ID");
		dictionaryFile = "id-utf8.csv";
	}
}
