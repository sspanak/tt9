package io.github.sspanak.tt9.preferences;

import android.util.Log;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.definitions.English;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class PreferenceValidator {
	public static ArrayList<Integer> validateEnabledLanguages(T9Preferences prefs, ArrayList<Integer> enabledLanguageIds) {
		ArrayList<Language> validLanguages = LanguageCollection.getAll(enabledLanguageIds);
		ArrayList<Integer> validLanguageIds = new ArrayList<>();
		for (Language lang : validLanguages) {
			validLanguageIds.add(lang.getId());
		}
		if (validLanguageIds.size() == 0) {
			validLanguageIds.add(1);
			Log.e("validateLanguage", "The language list seems to be corrupted. Resetting to first language only.");
		}

		prefs.setEnabledLanguages(validLanguageIds);

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
		Log.e("validateLanguage", error + "Enforcing language: " + validLanguage.getId());

		prefs.setInputLanguage(validLanguage.getId());
		return validLanguage;
	}
}
