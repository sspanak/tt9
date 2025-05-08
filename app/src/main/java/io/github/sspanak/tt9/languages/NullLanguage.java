package io.github.sspanak.tt9.languages;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

public class NullLanguage extends Language {
	public NullLanguage() {
		abcString = "ABC";
		code = "";
		currency = "";
		dictionaryFile = "";
		hasUpperCase = false;
		locale = Locale.ROOT;
		name = "Nulla Lingua";
	}

	@Override
	public boolean isValidWord(String word) {
		return false;
	}

	@NonNull
	@Override
	public ArrayList<String> getKeyCharacters(int key) {
		return new ArrayList<>();
	}

	@NonNull
	@Override
	public String getDigitSequenceForWord(String word) {
		return "";
	}
}
