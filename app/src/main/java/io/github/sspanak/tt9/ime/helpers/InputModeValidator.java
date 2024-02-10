package io.github.sspanak.tt9.ime.helpers;

import android.content.Context;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class InputModeValidator {
	public static ArrayList<Integer> validateEnabledLanguages(Context context, ArrayList<Integer> enabledLanguageIds) {
		ArrayList<Language> validLanguages = LanguageCollection.getAll(context, enabledLanguageIds);
		ArrayList<Integer> validLanguageIds = new ArrayList<>();
		for (Language lang : validLanguages) {
			validLanguageIds.add(lang.getId());
		}
		if (validLanguageIds.size() == 0) {
			validLanguageIds.add(LanguageCollection.getDefault(context).getId());
			Logger.e("validateEnabledLanguages", "The language list seems to be corrupted. Resetting to first language only.");
		}

		return validLanguageIds;
	}

	public static Language validateLanguage(Context context, Language language, ArrayList<Integer> validLanguageIds) {
		if (language != null && validLanguageIds.contains(language.getId())) {
			return language;
		}

		String error = language != null ? "Language: " + language.getId() + " is not enabled." : "Invalid language.";

		Language validLanguage = LanguageCollection.getLanguage(context, validLanguageIds.get(0));
		validLanguage = validLanguage != null ? validLanguage : LanguageCollection.getDefault(context);

		Logger.w("validateLanguage", error + " Enforcing language: " + validLanguage.getId());

		return validLanguage;
	}

	public static int validateMode(int oldModeId, ArrayList<Integer> allowedModes) {
		int newModeId = InputMode.MODE_123;

		if (allowedModes.contains(oldModeId)) {
			newModeId = oldModeId;
		} else if (allowedModes.contains(InputMode.MODE_ABC)) {
			newModeId = InputMode.MODE_ABC;
		} else if (allowedModes.size() > 0) {
			newModeId = allowedModes.get(0);
		}

		if (newModeId != oldModeId) {
			Logger.w("validateMode", "Invalid input mode: " + oldModeId + " Enforcing: " + newModeId);
		}

		return newModeId;
	}

	public static void validateTextCase(InputMode inputMode, int newTextCase) {
		if (!inputMode.setTextCase(newTextCase)) {
			inputMode.defaultTextCase();
			Logger.w("validateTextCase", "Invalid text case: " + newTextCase + " Enforcing: " + inputMode.getTextCase());
		}
	}
}
