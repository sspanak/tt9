package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;

class SettingsPunctuation extends SettingsInput {
	private final static String KEY_PREFIX_PUNCTUATION = "pref_punctuation_";
	private final static String KEY_PREFIX_SPECIAL = "pref_special_chars_";
	public final static char[] MANDATORY_PUNCTUATION = new char[] {'\'', '"', '-'};
	public final static char[] MANDATORY_SPECIAL_CHARS = new char[] {' ', '\n'};


	SettingsPunctuation(Context context) {
		super(context);
	}


	public void savePunctuation(@NonNull Language language, @NonNull String punctuation) {
		prefsEditor.putString(KEY_PREFIX_PUNCTUATION + language.getId(), punctuation);
		prefsEditor.apply();
	}


	public void saveSpecialChars(@NonNull Language language, @NonNull String specialChars) {
		String safeChars = specialChars.replace("\n", "⏎");
		prefsEditor.putString(KEY_PREFIX_SPECIAL + language.getId(), safeChars);
		prefsEditor.apply();
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
			prefs.getString(KEY_PREFIX_PUNCTUATION + language.getId(), null),
			language.getKeyCharacters(1)
		);
	}


	@NonNull
	public ArrayList<String> getSpecialCharsAsList(Language language) {
		if (language == null) {
			return new ArrayList<>();
		}

		String safeChars = prefs.getString(KEY_PREFIX_SPECIAL + language.getId(), null);

		return getCharsAsList(
			safeChars == null ? null : safeChars.replace("⏎", "\n"),
			language.getKeyCharacters(0)
		);
	}


	@NonNull
	public ArrayList<String> getOrderedKeyChars(Language language, int number) {
		ArrayList<String> orderedChars = new ArrayList<>();
		if (language == null) {
			return orderedChars;
		}

		if (number == 0) {
			orderedChars = getSpecialCharsAsList(language);
		} else if (number == 1) {
			orderedChars = getPunctuationAsList(language);
		}

		if (orderedChars.isEmpty()) {
			orderedChars = language.getKeyCharacters(number);
		}

		return orderedChars;
	}


	@NonNull
	public ArrayList<String> getOrderedKeyChars(Language language, int number, int group) {
		if (group > 0 && language != null) {
			return language.getKeyCharacters(number, group);
		}

		return getOrderedKeyChars(language, number);
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
