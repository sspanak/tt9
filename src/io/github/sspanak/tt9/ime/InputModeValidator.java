package io.github.sspanak.tt9.ime;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.definitions.English;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.T9Preferences;

public class InputModeValidator {
	public static ArrayList<Integer> validateEnabledLanguages(T9Preferences prefs, ArrayList<Integer> enabledLanguageIds) {
		ArrayList<Language> validLanguages = LanguageCollection.getAll(enabledLanguageIds);
		ArrayList<Integer> validLanguageIds = new ArrayList<>();
		for (Language lang : validLanguages) {
			validLanguageIds.add(lang.getId());
		}
		if (validLanguageIds.size() == 0) {
			validLanguageIds.add(1);
			Logger.e("tt9/validateEnabledLanguages", "The language list seems to be corrupted. Resetting to first language only.");
		}

		prefs.saveEnabledLanguages(validLanguageIds);

		return validLanguageIds;
	}

	public static Language validateLanguage(T9Preferences prefs, Language language, ArrayList<Integer> validLanguageIds) {
		if (language != null && validLanguageIds.contains(language.getId())) {
			return language;
		}

		String error = language != null ? "Language: " + language.getId() + " is not enabled." : "Invalid language.";

		Language validLanguage = LanguageCollection.getLanguage(validLanguageIds.get(0));
		validLanguage = validLanguage == null ? LanguageCollection.getLanguage(1) : validLanguage;
		validLanguage = validLanguage == null ? new English() : validLanguage;
		prefs.saveInputLanguage(validLanguage.getId());

		Logger.w("tt9/validateSavedLanguage", error + " Enforcing language: " + validLanguage.getId());

		return validLanguage;
	}

	public static int validateMode(T9Preferences prefs, int inputMode, ArrayList<Integer> allowedModes) {
		if (allowedModes.size() > 0 && allowedModes.contains(inputMode)) {
			return inputMode;
		}

		int newMode = allowedModes.size() > 0 ? allowedModes.get(0) : T9Preferences.MODE_123;
		prefs.saveInputMode(newMode);

		if (newMode != inputMode) {
			Logger.w("tt9/validateMode", "Invalid input mode: " + inputMode + " Enforcing: " + newMode);
		}

		return newMode;
	}

	public static int validateTextCase(T9Preferences prefs, int textCase, ArrayList<Integer> allowedTextCases) {
		if (allowedTextCases.size() > 0 && allowedTextCases.contains(textCase)) {
			return textCase;
		}

		int newCase = allowedTextCases.size() > 0 ? allowedTextCases.get(0) : T9Preferences.CASE_LOWER;
		prefs.saveTextCase(newCase);

		if (textCase != newCase) {
			Logger.w("tt9/validateTextCase", "Invalid text case: " + textCase + " Enforcing: " + newCase);
		}

		return newCase;
	}
}
