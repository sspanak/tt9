package io.github.sspanak.tt9.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.LanguageCollection;


public class T9Preferences {
	public static final int MAX_LANGUAGES = 32;

	private static T9Preferences self;

	private final SharedPreferences prefs;
	private final SharedPreferences.Editor prefsEditor;

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

	/************* VALIDATORS *************/

	private boolean doesLanguageExist(int langId) {
		return LanguageCollection.getLanguage(langId) != null;
	}

	private boolean isLanguageInRange(int langId) {
		return langId > 0 && langId <= MAX_LANGUAGES;
	}

	private boolean validateSavedLanguage(int langId, String logTag) {
		if (!doesLanguageExist(langId)) {
			Logger.w(logTag, "Not saving invalid language with ID: " + langId);
			return false;
		}

		if (!isLanguageInRange(langId)) {
			Logger.w(logTag, "Valid language ID range is [0, 31]. Not saving out-of-range language: " + langId);
			return false;
		}

		return true;
	}

	private boolean isIntInList(int number, ArrayList<Integer> list, String logTag, String logMsg) {
		if (!list.contains(number)) {
			Logger.w(logTag, logMsg);
			return false;
		}

		return true;
	}


	/************* PREFERENCES OPERATIONS *************/

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
		for (int langId : languageIds) {
			if (!validateSavedLanguage(langId, "tt9/saveEnabledLanguages")){
				continue;
			}

			int languageMaskBit = 1 << (langId - 1);
			languageMask |= languageMaskBit;
		}

		prefsEditor.putInt("pref_enabled_languages", languageMask);
		prefsEditor.apply();
	}

	public int getTextCase() {
		return prefs.getInt("pref_text_case", TraditionalT9.CASE_LOWER);
	}

	public void saveTextCase(int textCase) {
		boolean isTextCaseValid = isIntInList(
			textCase,
			new ArrayList<>(Arrays.asList(TraditionalT9.CASE_CAPITALIZE, TraditionalT9.CASE_LOWER, TraditionalT9.CASE_UPPER)),
			"tt9/saveTextCase",
			"Not saving invalid text case: " + textCase
		);

		if (isTextCaseValid) {
			prefsEditor.putInt("pref_text_case", textCase);
			prefsEditor.apply();
		}
	}


	public int getInputLanguage() {
		return prefs.getInt("pref_input_language", 1);
	}

	public void saveInputLanguage(int language) {
		if (validateSavedLanguage(language, "tt9/saveInputLanguage")){
			prefsEditor.putInt("pref_input_language", language);
			prefsEditor.apply();
		}
	}

	public int getInputMode() {
		return prefs.getInt("pref_input_mode", TraditionalT9.MODE_PREDICTIVE);
	}

	public void saveInputMode(int mode) {
		boolean isModeValid = isIntInList(
			mode,
			new ArrayList<>(Arrays.asList(TraditionalT9.MODE_123, TraditionalT9.MODE_ABC, TraditionalT9.MODE_PREDICTIVE)),
			"tt9/saveInputMode",
			"Not saving invalid text case: " + mode
		);

		if (isModeValid) {
			prefsEditor.putInt("pref_input_mode", mode);
			prefsEditor.apply();
		}
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

	public void clearLastWord() {
		this.saveLastWord("");
	}

}
