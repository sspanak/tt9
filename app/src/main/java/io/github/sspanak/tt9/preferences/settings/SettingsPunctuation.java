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


	public boolean isMandatoryPunctuationInList(String chars) {
		int foundCharacters = 0;
		for (char c : mandatoryPunctuation) {
			for (int i = 0; chars != null && i < chars.length(); i++) {
				if (chars.charAt(i) == c) {
					foundCharacters++;
					break;
				}
			}
		}

		return foundCharacters == mandatoryPunctuation.length;
	}


	public boolean areMandatorySpecialCharsInList(String chars) {
		int foundCharacters = 0;
		for (char c : mandatorySpecialChars) {
			for (int i = 0; chars != null && i < chars.length(); i++) {
				if (chars.charAt(i) == c) {
					foundCharacters++;
					break;
				}
			}
		}

		return foundCharacters == mandatorySpecialChars.length;
	}


	public boolean areSpecialCharsOrdered(String chars) {
		if (chars == null || chars.length() < mandatorySpecialChars.length) {
			return false;
		}

		for (int i = 0; i < mandatorySpecialChars.length; i++) {
			if (chars.charAt(i) != mandatorySpecialChars[i]) {
				return false;
			}
		}

		return true;
	}


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
