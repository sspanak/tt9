package io.github.sspanak.tt9.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.items.SectionKeymap;


public class SettingsStore {

	private final Context context;
	private final SharedPreferences prefs;
	private final SharedPreferences.Editor prefsEditor;


	public SettingsStore(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefsEditor = prefs.edit();
	}


	/************* validators *************/

	private boolean doesLanguageExist(int langId) {
		return LanguageCollection.getLanguage(context, langId) != null;
	}

	private boolean validateSavedLanguage(int langId, String logTag) {
		if (!doesLanguageExist(langId)) {
			Logger.w(logTag, "Not saving invalid language with ID: " + langId);
			return false;
		}

		return true;
	}

	@SuppressWarnings("SameParameterValue")
	private boolean isIntInList(int number, ArrayList<Integer> list, String logTag, String logMsg) {
		if (!list.contains(number)) {
			Logger.w(logTag, logMsg);
			return false;
		}

		return true;
	}


	/************* input settings *************/

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
			if (!validateSavedLanguage(Integer.parseInt(langId), "tt9/saveEnabledLanguageIds")){
				continue;
			}

			validLanguageIds.add(langId);
		}

		if (validLanguageIds.size() == 0) {
			Logger.w("tt9/saveEnabledLanguageIds", "Refusing to save an empty language list");
			return;
		}

		prefsEditor.putStringSet("pref_languages", validLanguageIds);
		prefsEditor.apply();
	}


	public int getTextCase() {
		return prefs.getInt("pref_text_case", InputMode.CASE_LOWER);
	}

	public void saveTextCase(int textCase) {
		boolean isTextCaseValid = isIntInList(
			textCase,
			new ArrayList<>(Arrays.asList(InputMode.CASE_CAPITALIZE, InputMode.CASE_LOWER, InputMode.CASE_UPPER)),
			"tt9/saveTextCase",
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
		if (validateSavedLanguage(language, "tt9/saveInputLanguage")){
			prefsEditor.putInt("pref_input_language", language);
			prefsEditor.apply();
		}
	}


	public int getInputMode() {
		return prefs.getInt("pref_input_mode", InputMode.MODE_PREDICTIVE);
	}

	public void saveInputMode(int mode) {
		boolean isModeValid = isIntInList(
			mode,
			new ArrayList<>(Arrays.asList(InputMode.MODE_123, InputMode.MODE_PREDICTIVE, InputMode.MODE_ABC)),
			"tt9/saveInputMode",
			"Not saving invalid input mode: " + mode
		);

		if (isModeValid) {
			prefsEditor.putInt("pref_input_mode", mode);
			prefsEditor.apply();
		}
	}


	/************* function key settings *************/

	public boolean areHotkeysInitialized() {
		return !prefs.getBoolean("hotkeys_initialized", false);
	}

	public void setDefaultKeys(
		int addWord,
		int backspace,
		int filterClear,
		int filterSuggestions,
		int previousSuggestion,
		int nextSuggestion,
		int nextInputMode,
		int nextLanguage,
		int showSettings
	) {
		prefsEditor
			.putString(SectionKeymap.ITEM_ADD_WORD, String.valueOf(addWord))
			.putString(SectionKeymap.ITEM_BACKSPACE, String.valueOf(backspace))
			.putString(SectionKeymap.ITEM_FILTER_CLEAR, String.valueOf(filterClear))
			.putString(SectionKeymap.ITEM_FILTER_SUGGESTIONS, String.valueOf(filterSuggestions))
			.putString(SectionKeymap.ITEM_PREVIOUS_SUGGESTION, String.valueOf(previousSuggestion))
			.putString(SectionKeymap.ITEM_NEXT_SUGGESTION, String.valueOf(nextSuggestion))
			.putString(SectionKeymap.ITEM_NEXT_INPUT_MODE, String.valueOf(nextInputMode))
			.putString(SectionKeymap.ITEM_NEXT_LANGUAGE, String.valueOf(nextLanguage))
			.putString(SectionKeymap.ITEM_SHOW_SETTINGS, String.valueOf(showSettings))
			.putBoolean("hotkeys_initialized", true)
			.apply();
	}

	public int getFunctionKey(String functionName) {
		try {
			return Integer.parseInt(prefs.getString(functionName, "0"));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public int getKeyAddWord() {
		return getFunctionKey(SectionKeymap.ITEM_ADD_WORD);
	}
	public int getKeyBackspace() {
		return getFunctionKey(SectionKeymap.ITEM_BACKSPACE);
	}
	public int getKeyFilterClear() {
		return getFunctionKey(SectionKeymap.ITEM_FILTER_CLEAR);
	}
	public int getKeyFilterSuggestions() {
		return getFunctionKey(SectionKeymap.ITEM_FILTER_SUGGESTIONS);
	}
	public int getKeyPreviousSuggestion() {
		return getFunctionKey(SectionKeymap.ITEM_PREVIOUS_SUGGESTION);
	}
	public int getKeyNextSuggestion() {
		return getFunctionKey(SectionKeymap.ITEM_NEXT_SUGGESTION);
	}
	public int getKeyNextInputMode() {
		return getFunctionKey(SectionKeymap.ITEM_NEXT_INPUT_MODE);
	}
	public int getKeyNextLanguage() {
		return getFunctionKey(SectionKeymap.ITEM_NEXT_LANGUAGE);
	}
	public int getKeyShowSettings() {
		return getFunctionKey(SectionKeymap.ITEM_SHOW_SETTINGS);
	}


	/************* UI settings *************/

	public boolean getDarkTheme() { return prefs.getBoolean("pref_dark_theme", true); }

	public boolean getShowSoftKeys() { return prefs.getBoolean("pref_show_soft_keys", true); }

	public boolean getShowSoftNumpad() { return getShowSoftKeys() && prefs.getBoolean("pref_show_soft_numpad", false); }

	/************* typing settings *************/

	public int getAbcAutoAcceptTimeout() { return prefs.getBoolean("abc_auto_accept", true) ? 800 : -1; }
	public boolean getAutoSpace() { return prefs.getBoolean("auto_space", true); }
	public boolean getAutoTextCase() { return prefs.getBoolean("auto_text_case", true); }
	public String getDoubleZeroChar() {
		String character = prefs.getString("pref_double_zero_char", ".");

		// SharedPreferences return a corrupted string when using the real "\n"... :(
		return  character.equals("\\n") ? "\n" : character;
	}
	public boolean getUpsideDownKeys() { return prefs.getBoolean("pref_upside_down_keys", false); }


	/************* internal settings *************/

	public int getDictionaryImportProgressUpdateInterval() { return 250; /* ms */ }
	public int getDictionaryImportWordChunkSize() { return 1000; /* words */ }

	public int getDictionaryMissingWarningInterval() { return 30000; /* ms */ }

	public int getSuggestionsMax() { return 20; }
	public int getSuggestionsMin() { return 8; }

	public int getSuggestionSelectAnimationDuration() { return 66; }
	public int getSuggestionTranslateAnimationDuration() { return 0; }

	public int getSoftKeyRepeatDelay() { return 40; /* ms */ }

	public int getWordFrequencyMax() { return 25500; }
	public int getWordFrequencyNormalizationDivider() { return 100; } // normalized frequency = getWordFrequencyMax() / getWordFrequencyNormalizationDivider()


	/************* add word, last word *************/

	public String getLastWord() {
		return prefs.getString("last_word", "");
	}

	public void saveLastWord(String lastWord) {
		// "last_word" was part of the original Settings implementation.
		// It is weird, but it is simple and it works, so I decided to keep it.
		prefsEditor.putString("last_word", lastWord);
		prefsEditor.apply();
	}

	public void clearLastWord() {
		this.saveLastWord("");
	}
}
