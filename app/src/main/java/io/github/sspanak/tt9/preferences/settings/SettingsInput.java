package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.util.Logger;

class SettingsInput extends SettingsHacks {
	SettingsInput(Context context) { super(context); }


	public boolean areEnabledLanguagesMoreThanN(int N) {
		final Set<String> langs = prefs.getStringSet("pref_languages", null);
		return langs != null && langs.size() > N;
	}


	@NonNull
	public ArrayList<Integer> getEnabledLanguageIds() {
		final Set<String> rawLangIds = prefs.getStringSet("pref_languages", null);
		final HashSet<String> langIds = new HashSet<>(rawLangIds != null ? rawLangIds : Collections.emptySet());

		final ArrayList<Integer> list = new ArrayList<>();
		for (String languageId : langIds) {
			try {
				list.add(Integer.parseInt(languageId));
			} catch (NumberFormatException e) {
				Logger.w(LOG_TAG, "Ignoring invalid language ID in preferences: '" + languageId + "'");
			}
		}

		if (list.isEmpty()) {
			list.add(LanguageCollection.getDefault().getId());
		}

		return list;
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
			if (!Validators.validateInputLanguage(Integer.parseInt(langId), "saveEnabledLanguageIds")){
				continue;
			}

			validLanguageIds.add(langId);
		}

		if (validLanguageIds.isEmpty()) {
			Logger.w(LOG_TAG, "Refusing to save an empty language list");
			return;
		}

		getPrefsEditor().putStringSet("pref_languages", validLanguageIds);
		getPrefsEditor().apply();
	}


	public int getInputLanguage() {
		return prefs.getInt("pref_input_language", LanguageCollection.getDefault().getId());
	}


	public void saveInputLanguage(int language) {
		if (Validators.validateInputLanguage(language, "saveInputLanguage")){
			getPrefsEditor().putInt("pref_input_language", language);
			getPrefsEditor().apply();
		}
	}


	public int getInputMode() {
		return prefs.getInt("pref_input_mode", Validators.DEFAULT_INPUT_MODE);
	}


	public void saveInputMode(int mode) {
		boolean isModeValid = Validators.validateInputMode(mode, LOG_TAG, "Not saving invalid input mode: " + mode);
		if (isModeValid) {
			getPrefsEditor().putInt("pref_input_mode", mode);
			getPrefsEditor().apply();
		}
	}


	public int getTextCase() {
		return prefs.getInt("pref_text_case", Validators.DEFAULT_TEXT_CASE);
	}


	public void saveTextCase(int textCase) {
		boolean isTextCaseValid = Validators.validateTextCase(textCase, LOG_TAG,"Not saving invalid text case: " + textCase);
		if (isTextCaseValid) {
			getPrefsEditor().putInt("pref_text_case", textCase);
			getPrefsEditor().apply();
		}
	}
}
