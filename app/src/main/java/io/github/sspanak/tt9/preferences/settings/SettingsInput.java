package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.util.Logger;

class SettingsInput extends SettingsHotkeys {
	SettingsInput(Context context) { super(context); }


	public ArrayList<Integer> getEnabledLanguageIds() {
		Set<String> languagesPref = getEnabledLanguagesIdsAsStrings();

		ArrayList<Integer>languageIds = new ArrayList<>();
		for (String languageId : languagesPref) {
			languageIds.add(Integer.valueOf(languageId));
		}

		return languageIds;
	}


	public Set<String> getEnabledLanguagesIdsAsStrings() {
		return prefs.getStringSet("pref_languages", new HashSet<>(Collections.singletonList(
			String.valueOf(LanguageCollection.getDefault(context).getId())
		)));
	}


	public void saveEnabledLanguageIds(ArrayList<Integer> languageIds) {
		Set<String> idsAsStrings = new HashSet<>();
		for (int langId : languageIds) {
			idsAsStrings.add(String.valueOf(langId));
		}

		saveEnabledLanguageIds(idsAsStrings);
	}


	public void saveEnabledLanguageIds(Set<String> languageIds) {
		Set<String> validLanguageIds = new HashSet<>();

		for (String langId : languageIds) {
			if (!Validators.validateSavedLanguage(context, Integer.parseInt(langId), "saveEnabledLanguageIds")){
				continue;
			}

			validLanguageIds.add(langId);
		}

		if (validLanguageIds.isEmpty()) {
			Logger.w("saveEnabledLanguageIds", "Refusing to save an empty language list");
			return;
		}

		prefsEditor.putStringSet("pref_languages", validLanguageIds);
		prefsEditor.apply();
	}


	public int getTextCase() {
		return prefs.getInt("pref_text_case", InputMode.CASE_LOWER);
	}

	public void saveTextCase(int textCase) {
		boolean isTextCaseValid = Validators.isIntInList(
			textCase,
			new ArrayList<>(Arrays.asList(InputMode.CASE_CAPITALIZE, InputMode.CASE_LOWER, InputMode.CASE_UPPER)),
			"saveTextCase",
			"Not saving invalid text case: " + textCase
		);

		if (isTextCaseValid) {
			prefsEditor.putInt("pref_text_case", textCase);
			prefsEditor.apply();
		}
	}


	public int getInputLanguage() {
		return prefs.getInt("pref_input_language", LanguageCollection.getDefault(context).getId());
	}


	public void saveInputLanguage(int language) {
		if (Validators.validateSavedLanguage(context, language, "saveInputLanguage")){
			prefsEditor.putInt("pref_input_language", language);
			prefsEditor.apply();
		}
	}


	public int getInputMode() {
		return prefs.getInt("pref_input_mode", InputMode.MODE_PREDICTIVE);
	}


	public void saveInputMode(int mode) {
		boolean isModeValid = Validators.isIntInList(
			mode,
			new ArrayList<>(Arrays.asList(InputMode.MODE_123, InputMode.MODE_PREDICTIVE, InputMode.MODE_ABC)),
			"saveInputMode",
			"Not saving invalid input mode: " + mode
		);

		if (isModeValid) {
			prefsEditor.putInt("pref_input_mode", mode);
			prefsEditor.apply();
		}
	}
}
