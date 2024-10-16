package io.github.sspanak.tt9.languages;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

public class NullLanguage extends Language {
	public NullLanguage() {
		locale = Locale.ROOT;
		name = "Nulla Lingua";
		abcString = "ABC";
		code = "";
		dictionaryFile = "";
		hasUpperCase = false;
	}

	@NonNull
	@Override
	public ArrayList<String> getKeyCharacters(int key, int characterGroup) {
		return new ArrayList<>();
	}

	@Override
	public boolean isValidWord(String word) {
		return false;
	}

	@NonNull
	@Override
	public String getDigitSequenceForWord(String word) {
		return "";
	}
}
