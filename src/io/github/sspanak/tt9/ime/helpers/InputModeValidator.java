package io.github.sspanak.tt9.ime.helpers;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class InputModeValidator {
	public static ArrayList<Integer> validateEnabledLanguages(SettingsStore settings, ArrayList<Integer> enabledLanguageIds) {
		ArrayList<Language> validLanguages = LanguageCollection.getAll(enabledLanguageIds);
		ArrayList<Integer> validLanguageIds = new ArrayList<>();
		for (Language lang : validLanguages) {
			validLanguageIds.add(lang.getId());
		}
		if (validLanguageIds.size() == 0) {
			validLanguageIds.add(LanguageCollection.getDefault().getId());
			Logger.e("tt9/validateEnabledLanguages", "The language list seems to be corrupted. Resetting to first language only.");
		}

		return validLanguageIds;
	}

	public static Language validateLanguage(SettingsStore settings, Language language, ArrayList<Integer> validLanguageIds) {
		if (language != null && validLanguageIds.contains(language.getId())) {
			return language;
		}

		String error = language != null ? "Language: " + language.getId() + " is not enabled." : "Invalid language.";

		Language validLanguage = LanguageCollection.getLanguage(validLanguageIds.get(0));
		validLanguage = validLanguage != null ? validLanguage : LanguageCollection.getDefault();

		Logger.w("tt9/validateLanguage", error + " Enforcing language: " + validLanguage.getId());

		return validLanguage;
	}

	public static int validateMode(SettingsStore settings, int oldModeId, ArrayList<Integer> allowedModes) {
		int newModeId = InputMode.MODE_123;

		if (allowedModes.contains(oldModeId)) {
			newModeId = oldModeId;
		} else if (allowedModes.contains(InputMode.MODE_ABC)) {
			newModeId = InputMode.MODE_ABC;
		} else if (allowedModes.size() > 0) {
			newModeId = allowedModes.get(0);
		}

		if (newModeId != oldModeId) {
			Logger.w("tt9/validateMode", "Invalid input mode: " + oldModeId + " Enforcing: " + newModeId);
		}

		return newModeId;
	}

	public static void validateTextCase(SettingsStore settings, InputMode inputMode, int newTextCase) {
		if (!inputMode.setTextCase(newTextCase)) {
			inputMode.defaultTextCase();
			Logger.w("tt9/validateTextCase", "Invalid text case: " + newTextCase + " Enforcing: " + inputMode.getTextCase());
		}
	}
}
