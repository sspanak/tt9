package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.view.KeyEvent;

import io.github.sspanak.tt9.preferences.screens.hotkeys.SectionKeymap;

class SettingsHotkeys extends SettingsHacks {
	SettingsHotkeys(Context context) { super(context); }

	public boolean areHotkeysInitialized() {
		return !prefs.getBoolean("hotkeys_v2_initialized", false);
	}

	public void setDefaultKeys(
		int backspace,
		int comandPalette,
		int filterClear,
		int filterSuggestions,
		int previousSuggestion,
		int nextSuggestion,
		int nextInputMode,
		int nextLanguage
	) {
		prefsEditor
			.putString(SectionKeymap.ITEM_BACKSPACE, String.valueOf(backspace))
			.putString(SectionKeymap.ITEM_COMMAND_PALETTE, String.valueOf(comandPalette))
			.putString(SectionKeymap.ITEM_FILTER_CLEAR, String.valueOf(filterClear))
			.putString(SectionKeymap.ITEM_FILTER_SUGGESTIONS, String.valueOf(filterSuggestions))
			.putString(SectionKeymap.ITEM_PREVIOUS_SUGGESTION, String.valueOf(previousSuggestion))
			.putString(SectionKeymap.ITEM_NEXT_SUGGESTION, String.valueOf(nextSuggestion))
			.putString(SectionKeymap.ITEM_NEXT_INPUT_MODE, String.valueOf(nextInputMode))
			.putString(SectionKeymap.ITEM_NEXT_LANGUAGE, String.valueOf(nextLanguage))
			.putBoolean("hotkeys_v2_initialized", true)
			.apply();
	}


	public int getFunctionKey(String functionName) {
		return getStringifiedInt(functionName, KeyEvent.KEYCODE_UNKNOWN);
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
	public int getKeyCommandPalette() {
		return getFunctionKey(SectionKeymap.ITEM_COMMAND_PALETTE);
	}
}
