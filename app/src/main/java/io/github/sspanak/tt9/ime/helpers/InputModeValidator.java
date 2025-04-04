package io.github.sspanak.tt9.ime.helpers;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.util.Logger;

public class InputModeValidator {
	public static ArrayList<Integer> validateEnabledLanguages(ArrayList<Integer> enabledLanguageIds) {
		ArrayList<Language> validLanguages = LanguageCollection.getAll(enabledLanguageIds);
		ArrayList<Integer> validLanguageIds = new ArrayList<>();
		for (Language lang : validLanguages) {
			validLanguageIds.add(lang.getId());
		}
		if (validLanguageIds.isEmpty()) {
			validLanguageIds.add(LanguageCollection.getDefault().getId());
			Logger.e("validateEnabledLanguages", "The language list seems to be corrupted. Resetting to first language only.");
		}

		return validLanguageIds;
	}

	public static Language validateLanguage(Language language, ArrayList<Integer> validLanguageIds) {
		if (language != null && validLanguageIds.contains(language.getId())) {
			return language;
		}

		String error = language != null ? "Language: " + language.getId() + " is not enabled." : "Invalid language.";

		Language validLanguage = LanguageCollection.getLanguage(validLanguageIds.get(0));
		validLanguage = validLanguage != null ? validLanguage : LanguageCollection.getDefault();

		Logger.d("validateLanguage", error + " Enforcing language: " + validLanguage.getId());

		return validLanguage;
	}

	public static int validateMode(int oldModeId, ArrayList<Integer> allowedModes) {
		int newModeId = InputMode.MODE_123;

		if (allowedModes.contains(oldModeId)) {
			newModeId = oldModeId;
		} else if ((oldModeId == InputMode.MODE_HIRAGANA || oldModeId == InputMode.MODE_KATAKANA) && allowedModes.contains(InputMode.MODE_PREDICTIVE)) {
			newModeId = InputMode.MODE_PREDICTIVE;
		} else if (allowedModes.contains(InputMode.MODE_ABC)) {
			newModeId = InputMode.MODE_ABC;
		} else if (allowedModes.contains(InputMode.MODE_HIRAGANA)) {
			newModeId = InputMode.MODE_HIRAGANA;
		} else if (!allowedModes.isEmpty()) {
			newModeId = allowedModes.get(0);
		}

		if (newModeId != oldModeId) {
			Logger.d("validateMode", "Invalid input mode: " + oldModeId + " Enforcing: " + newModeId + " from " + allowedModes);
		}

		return newModeId;
	}

	public static void validateTextCase(InputMode inputMode, int newTextCase) {
		if (!inputMode.setTextCase(newTextCase)) {
			inputMode.defaultTextCase();
			Logger.d("validateTextCase", "Invalid text case: " + newTextCase + " Enforcing: " + inputMode.getTextCase());
		}
	}
}
