package io.github.sspanak.tt9.languages;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import io.github.sspanak.tt9.R;

public class NullLanguage extends Language {
	public NullLanguage(Context context) {
		locale = Locale.ROOT;
		name = context.getString(R.string.no_language);
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
