package io.github.sspanak.tt9.ime;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.definitions.English;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class InputModeValidator {
	public static ArrayList<Integer> validateEnabledLanguages(SettingsStore settings, ArrayList<Integer> enabledLanguageIds) {
		ArrayList<Language> validLanguages = LanguageCollection.getAll(enabledLanguageIds);
		ArrayList<Integer> validLanguageIds = new ArrayList<>();
		for (Language lang : validLanguages) {
			validLanguageIds.add(lang.getId());
		}
		if (validLanguageIds.size() == 0) {
			validLanguageIds.add(1);
			Logger.e("tt9/validateEnabledLanguages", "The language list seems to be corrupted. Resetting to first language only.");
		}

		settings.saveEnabledLanguageIds(validLanguageIds);

		return validLanguageIds;
	}

	public static Language validateLanguage(SettingsStore settings, Language language, ArrayList<Integer> validLanguageIds) {
		if (language != null && validLanguageIds.contains(language.getId())) {
			return language;
		}

		String error = language != null ? "Language: " + language.getId() + " is not enabled." : "Invalid language.";

		Language validLanguage = LanguageCollection.getLanguage(validLanguageIds.get(0));
		validLanguage = validLanguage == null ? LanguageCollection.getLanguage(1) : validLanguage;
		validLanguage = validLanguage == null ? new English() : validLanguage;
		settings.saveInputLanguage(validLanguage.getId());

		Logger.w("tt9/validateSavedLanguage", error + " Enforcing language: " + validLanguage.getId());

		return validLanguage;
	}

	public static InputMode validateMode(SettingsStore settings, InputMode inputMode, ArrayList<Integer> allowedModes) {
		if (allowedModes.size() > 0 && allowedModes.contains(inputMode.getId())) {
			return inputMode;
		}

		InputMode newMode = InputMode.getInstance(allowedModes.size() > 0 ? allowedModes.get(0) : InputMode.MODE_123);
		settings.saveInputMode(newMode);

		if (newMode.getId() != inputMode.getId()) {
			Logger.w("tt9/validateMode", "Invalid input mode: " + inputMode.getId() + " Enforcing: " + newMode.getId());
		}

		return newMode;
	}

	public static void validateTextCase(SettingsStore settings, InputMode inputMode, int newTextCase) {
		if (!inputMode.setTextCase(newTextCase)) {
			inputMode.defaultTextCase();
			Logger.w("tt9/validateTextCase", "Invalid text case: " + newTextCase + " Enforcing: " + inputMode.getTextCase());
		}

		settings.saveTextCase(inputMode.getTextCase());
	}
}
