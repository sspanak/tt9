package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import io.github.sspanak.tt9.preferences.screens.hotkeys.SectionKeymap;

class SettingsHotkeys extends SettingsHacks {
	SettingsHotkeys(Context context) { super(context); }

	public boolean areHotkeysInitialized() {
		return !prefs.getBoolean("hotkeys_initialized", false);
	}

	public void setDefaultKeys(
		int addWord,
		int backspace,
		int changeKeyboard,
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
			.putString(SectionKeymap.ITEM_CHANGE_KEYBOARD, String.valueOf(changeKeyboard))
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
	public int getKeyChangeKeyboard() {
		return getFunctionKey(SectionKeymap.ITEM_CHANGE_KEYBOARD);
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
}
