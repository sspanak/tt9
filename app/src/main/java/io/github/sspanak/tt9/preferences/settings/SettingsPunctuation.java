package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.ui.tray.SuggestionsBar;
import io.github.sspanak.tt9.util.chars.Characters;

class SettingsPunctuation extends SettingsInput {
	private final static String CHARS_1_PREFIX = "punctuation_order_key_1_initial_";
	public final static String CHARS_GROUP_1 = "punctuation_order_key_1_group";
	public final static String CHARS_AFTER_GROUP_1 = "punctuation_order_key_1_after_group";

	private final static String CHARS_0_PREFIX = "punctuation_order_key_0_initial_";
	public final static String CHARS_GROUP_0 = "punctuation_order_key_0_group";
	public final static String CHARS_AFTER_GROUP_0 = "punctuation_order_key_0_after_group";

	private final static char[] MANDATORY_CHARS_1_EU = new char[] {'\'', '"', '-'};
	public final static char[] FORBIDDEN_CHARS_0 = new char[] {' ', '\n', '\t'};


	SettingsPunctuation(Context context) {
		super(context);
	}


	public void setDefaultCharOrder(@NonNull Language language, boolean overwrite) {
		if (overwrite) {
			setIncludeNewlineInChars0(language, true);
			setIncludeTabInChars0(language, true);
		}

		if (overwrite || noDefault0Chars(language)) {
			String chars = new String(FORBIDDEN_CHARS_0) + String.join("", language.getKeyCharacters(0));
			chars = chars.replace(" ", Characters.getSpace(language));
			final int splitPosition = 7;
			saveChars0(language, String.join("", chars.substring(0, splitPosition)));
			saveCharsExtra(language, CHARS_GROUP_0, String.join("", Characters.getCurrencies(language)));
			saveCharsExtra(language, CHARS_AFTER_GROUP_0, chars.substring(splitPosition));
		}

		if (overwrite || noDefault1Chars(language)) {
			saveChars1(language, String.join("", language.getKeyCharacters(1)));
			saveCharsExtra(language, CHARS_GROUP_1, "");
			saveCharsExtra(language, CHARS_AFTER_GROUP_1, "");
		}
	}


	private boolean noDefault0Chars(@NonNull Language language) {
		return prefs.getString(CHARS_0_PREFIX + language.getId(), null) == null;
	}


	private boolean noDefault1Chars(@NonNull Language language) {
		return prefs.getString(CHARS_1_PREFIX + language.getId(), null) == null;
	}


	@NonNull
	public char[] getMandatoryChars0(@Nullable Language language) {
		return LanguageKind.isCyrillic(language) || LanguageKind.isLatinBased(language) ? MANDATORY_CHARS_1_EU : new char[0];
	}


	public void saveChars1(@NonNull Language language, @NonNull String chars) {
		prefsEditor.putString(CHARS_1_PREFIX + language.getId(), chars);
		prefsEditor.apply();
	}


	public void saveChars0(@NonNull Language language, @NonNull String chars) {
		String safeChars = chars
			.replace("\n", "⏎")
			.replace("\t", Characters.TAB);
		prefsEditor.putString(CHARS_0_PREFIX + language.getId(), safeChars);
		prefsEditor.apply();
	}


	public void saveCharsExtra(@NonNull Language language, @NonNull String listKey, @NonNull String chars) {
		prefsEditor.putString(listKey + "_" + language.getId(), chars);
		prefsEditor.apply();
	}


	@NonNull public String getChars1(@Nullable Language language) {
		return String.join("", getChars1AsList(language));
	}


	@NonNull public String getChars0(@Nullable Language language) {
		return String.join("", getChars0AsList(language));
	}


	@NonNull public String getCharsExtra(@NonNull Language language, @NonNull String listKey) {
		return prefs.getString(listKey + "_" + language.getId(), "");
	}


	@NonNull
	public ArrayList<String> getChars1AsList(@Nullable Language language) {
		if (language == null) {
			return new ArrayList<>();
		}

		return getCharsAsList(
			prefs.getString(CHARS_1_PREFIX + language.getId(), null),
			language.getKeyCharacters(1)
		);
	}


	@NonNull
	public ArrayList<String> getChars0AsList(@Nullable Language language) {
		if (language == null) {
			return new ArrayList<>();
		}

		String safeChars = prefs.getString(CHARS_0_PREFIX + language.getId(), null);
		if (safeChars != null) {
			safeChars = safeChars
				.replace("⏎", "\n")
				.replace(Characters.TAB, "\t")
				.replace("Tab", "\t") // also convert the legacy "Tab" string
				.replace(" ", Characters.getSpace(language));

		}

		return getCharsAsList(safeChars, language.getKeyCharacters(0));
	}


	@NonNull
	public ArrayList<String> getCharsExtraAsList(@NonNull Language language, @NonNull String listKey) {
		return getCharsAsList(getCharsExtra(language, listKey), new ArrayList<>());
	}


	@NonNull
	public ArrayList<String> getOrderedKeyChars(@Nullable Language language, int number) {
		if (language == null) {
			return new ArrayList<>();
		}

		ArrayList<String> chars;

		switch (number) {
			case 0 -> {
				chars = getChars0AsList(language);
				if (!getCharsExtra(language, CHARS_GROUP_0).isEmpty()) {
					chars.add(SuggestionsBar.SHOW_GROUP_0_SUGGESTION);
				}
				chars.addAll(getCharsExtraAsList(language, CHARS_AFTER_GROUP_0));
			}
			case 1 -> {
				chars = getChars1AsList(language);
				if (!getCharsExtra(language, CHARS_GROUP_1).isEmpty()) {
					chars.add(SuggestionsBar.SHOW_GROUP_1_SUGGESTION);
				}
				chars.addAll(getCharsExtraAsList(language, CHARS_AFTER_GROUP_1));
			}
			default -> {
				return language.getKeyCharacters(number);
			}
		}

		return chars;
	}


	@NonNull
	private ArrayList<String> getCharsAsList(@Nullable String chars, @NonNull ArrayList<String> defaultValue) {
		if (chars == null) {
			return defaultValue;
		}

		ArrayList<String> charsList = new ArrayList<>();
		for (int i = 0; i < chars.length(); i++) {
			charsList.add(String.valueOf(chars.charAt(i)));
		}

		return charsList;
	}

	public boolean getIncludeNewlineInChars0(@NonNull Language language) {
		return prefs.getBoolean("punctuation_order_include_newline_" + language.getId(), true);
	}

	public void setIncludeNewlineInChars0(@NonNull Language language, boolean include) {
		prefsEditor.putBoolean("punctuation_order_include_newline_" + language.getId(), include);
		prefsEditor.apply();
	}

	public boolean getIncludeTabInChars0(@NonNull Language language) {
		return prefs.getBoolean("punctuation_order_include_tab_" + language.getId(), true);
	}

	public void setIncludeTabInChars0(@NonNull Language language, boolean include) {
		prefsEditor.putBoolean("punctuation_order_include_tab_" + language.getId(), include);
		prefsEditor.apply();
	}
}
