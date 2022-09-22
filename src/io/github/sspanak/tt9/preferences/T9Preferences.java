package io.github.sspanak.tt9.preferences;

import static io.github.sspanak.tt9.preferences.PreferencesValidator.MAX_LANGUAGES;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Arrays;


public class T9Preferences {
	private static T9Preferences self;

	private final SharedPreferences prefs;
	private final SharedPreferences.Editor prefsEditor;

	public static final int CASE_LOWER = 0;
	public static final int CASE_CAPITALIZE = 1;
	public static final int CASE_UPPER = 2;

	public static final int MODE_PREDICTIVE = 0;
	public static final int MODE_ABC = 1;
	public static final int MODE_123 = 2;

	public T9Preferences (Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		prefsEditor = prefs.edit();
	}

	public static T9Preferences getInstance(Context context) {
		if (self == null) {
			self = new T9Preferences(context);
		}

		return self;
	}



	public ArrayList<Integer> getEnabledLanguages() {
		int languageMask = prefs.getInt("pref_enabled_languages", 1);
		ArrayList<Integer>languageIds = new ArrayList<>();

		for (int langId = 1; langId < MAX_LANGUAGES; langId++) {
			int maskBit = 1 << (langId - 1);
			if ((maskBit & languageMask) != 0) {
				languageIds.add(langId);
			}
		}

		return languageIds;
	}

	public void saveEnabledLanguages(ArrayList<Integer> languageIds) {
		int languageMask = 0;
		for (Integer langId : languageIds) {
			if (!PreferencesValidator.validateSavedLanguage(langId, "tt9/saveEnabledLanguages")){
				continue;
			}

			int languageMaskBit = 1 << (langId - 1);
			languageMask |= languageMaskBit;
		}

		prefsEditor.putInt("pref_enabled_languages", languageMask);
		prefsEditor.apply();
	}

	public int getTextCase() {
		return prefs.getInt("pref_text_case", CASE_LOWER);
	}

	public void saveTextCase(int textCase) {
		ArrayList<Integer> allCases = new ArrayList<>(Arrays.asList(CASE_CAPITALIZE, CASE_LOWER, CASE_UPPER));
		if (PreferencesValidator.validateSavedTextCase(textCase, allCases, "tt9/saveTextCase")) {
			prefsEditor.putInt("pref_text_case", textCase);
			prefsEditor.apply();
		}
	}


	public int getInputLanguage() {
		return prefs.getInt("pref_input_language", 0);
	}

	public void saveInputLanguage(int language) {
		if (PreferencesValidator.validateSavedLanguage(language, "tt9/saveInputLanguage")){
			prefsEditor.putInt("pref_input_language", language);
			prefsEditor.apply();
		}
	}

	public int getInputMode() {
		return prefs.getInt("pref_input_mode", MODE_PREDICTIVE);
	}

	public void saveInputMode(int mode) {
		prefsEditor.putInt("pref_input_mode", mode);
		prefsEditor.apply();
	}


	public int getKeyBackspace() {
		return prefs.getInt("pref_key_backspace", KeyEvent.KEYCODE_BACK);
	}

	public int getKeyInputMode() { return prefs.getInt("pref_key_input_mode", KeyEvent.KEYCODE_POUND); }

	public int getKeyOtherActions() { return prefs.getInt("pref_key_other_actions", KeyEvent.KEYCODE_STAR); }


	public int getSuggestionsMin() { return 8; }
	public int getSuggestionsMax() { return 20; }


	public String getLastWord() {
		return prefs.getString("last_word", "");
	}

	public void saveLastWord(String lastWord) {
		// "last_word" was part of the original Preferences implementation.
		// It is weird, but it is simple and it works, so I decided to keep it.
		prefsEditor.putString("last_word", lastWord);
		prefsEditor.apply();
	}
}
