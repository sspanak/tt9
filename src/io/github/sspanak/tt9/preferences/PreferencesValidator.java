package io.github.sspanak.tt9.preferences;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class PreferencesValidator {
	public static final int MAX_LANGUAGES = 32;

	public static boolean doesLanguageExist(int langId) {
		return LanguageCollection.getLanguage(langId) != null;
	}

	public static boolean isLanguageInRange(int langId) {
		return langId > 0 && langId <= MAX_LANGUAGES;
	}

	public static boolean validateSavedLanguage(int langId, String logTag) {
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

	public static boolean validateSavedTextCase(int textCase, ArrayList<Integer> allTextCases, String logTag) {
		if (!allTextCases.contains(textCase)) {
			Logger.w(logTag, "Not saving invalid text case: " + textCase);
			return false;
		}

		return true;
	}

}
