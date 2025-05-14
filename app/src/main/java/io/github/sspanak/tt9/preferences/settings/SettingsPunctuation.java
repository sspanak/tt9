package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.chars.Characters;

class SettingsPunctuation extends SettingsInput {
	private final static String CHARS_1_PREFIX = "pref_punctuation_";
	private final static String CHARS_0_PREFIX = "pref_special_chars_";
	private final static char[] MANDATORY_CHARS_1_EU = new char[] {'\'', '"', '-'};
	public final static char[] FORBIDDEN_CHARS_0 = new char[] {' ', '\n', '\t'};


	SettingsPunctuation(Context context) {
		super(context);
	}


	public char[] getMandatoryChars0(Language language) {
		return LanguageKind.isCyrillic(language) || LanguageKind.isLatinBased(language) ? MANDATORY_CHARS_1_EU : new char[0];
	}


	public void saveChars1(@NonNull Language language, @NonNull String punctuation) {
		prefsEditor.putString(CHARS_1_PREFIX + language.getId(), punctuation);
		prefsEditor.apply();
	}


	public void saveChars0(@NonNull Language language, @NonNull String specialChars) {
		String safeChars = specialChars
			.replace("\n", "⏎")
			.replace("\t", Characters.TAB);
		prefsEditor.putString(CHARS_0_PREFIX + language.getId(), safeChars);
		prefsEditor.apply();
	}


	@NonNull public String getChars1(Language language) {
		return String.join("", getChars1AsList(language));
	}


	@NonNull public String getChars0(Language language) {
		return String.join("", getChars0AsList(language));
	}


	@NonNull
	public ArrayList<String> getChars1AsList(Language language) {
		if (language == null) {
			return new ArrayList<>();
		}

		return getCharsAsList(
			prefs.getString(CHARS_1_PREFIX + language.getId(), null),
			language.getKeyCharacters(1)
		);
	}


	@NonNull
	public ArrayList<String> getChars0AsList(Language language) {
		if (language == null) {
			return new ArrayList<>();
		}

		String safeChars = prefs.getString(CHARS_0_PREFIX + language.getId(), null);

		return getCharsAsList(
			safeChars == null ? null : safeChars.replace("⏎", "\n").replace(Characters.TAB, "\t"),
			language.getKeyCharacters(0)
		);
	}


	@NonNull
	public ArrayList<String> getOrderedKeyChars(Language language, int number) {
		return switch (number) {
			case 0 -> getChars0AsList(language);
			case 1 -> getChars1AsList(language);
			default -> language != null ? language.getKeyCharacters(number) : new ArrayList<>();
		};
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
