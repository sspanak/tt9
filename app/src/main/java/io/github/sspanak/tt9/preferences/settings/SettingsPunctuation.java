package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;

class SettingsPunctuation extends SettingsInput {
	SettingsPunctuation(Context context) {
		super(context);
	}


	public final char[] mandatoryPunctuation = new char[] {'\'', '"', '-'};
	public final char[] mandatorySpecialChars = new char[] {' ', '\n'};


	public void savePunctuation(@NonNull Language language, @NonNull String punctuation) {
		prefsEditor.putString("pref_punctuation_" + language.getId(), punctuation);
	}


	public void saveSpecialChars(@NonNull Language language, @NonNull String specialChars) {
		prefsEditor.putString("pref_special_chars_" + language.getId(), specialChars);
	}


	@NonNull public String getPunctuation(Language language) {
		return String.join("", getPunctuationAsList(language));
	}


	@NonNull public String getSpecialChars(Language language) {
		return String.join("", getSpecialCharsAsList(language));
	}


	@NonNull
	public ArrayList<String> getPunctuationAsList(Language language) {
		if (language == null) {
			return new ArrayList<>();
		}

		return getCharsAsList(
			prefs.getString("pref_punctuation_" + language.getId(), null),
			language.getKeyCharacters(1)
		);
	}


	@NonNull public ArrayList<String> getSpecialCharsAsList(Language language) {
		if (language == null) {
			return new ArrayList<>();
		}

		return getCharsAsList(
			prefs.getString("pref_special_chars_" + language.getId(), null),
			language.getKeyCharacters(0)
		);
	}

	private ArrayList<String> getCharsAsList(String chars, ArrayList<String> defaultValue) {
		if (chars == null) {
			return defaultValue;
		}

		ArrayList<String> charsList = new ArrayList<>();
		for (int i = 0; i < chars.length(); i++) {
			charsList.add(String.valueOf(chars.charAt(i)));
		}

		return charsList;
	}
}
