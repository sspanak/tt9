package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import io.github.sspanak.tt9.util.Logger;

public class SettingsHotkeys extends SettingsUI {
	private static final String HOTKEY_VERSION = "hotkeys_v6";

	public static final String FUNC_ADD_WORD = "key_add_word";
	public static final String FUNC_BACKSPACE = "key_backspace";
	public static final String FUNC_COMMAND_PALETTE = "key_command_palette";
	public static final String FUNC_EDIT_TEXT = "key_edit_text";
	public static final String FUNC_FILTER_CLEAR = "key_filter_clear";
	public static final String FUNC_FILTER_SUGGESTIONS = "key_filter_suggestions";
	public static final String FUNC_PREVIOUS_SUGGESTION = "key_previous_suggestion";
	public static final String FUNC_NEXT_SUGGESTION = "key_next_suggestion";
	public static final String FUNC_NEXT_INPUT_MODE = "key_next_input_mode";
	public static final String FUNC_NEXT_LANGUAGE = "key_next_language";
	public static final String FUNC_SELECT_KEYBOARD = "key_select_keyboard";
	public static final String FUNC_SHIFT = "key_shift";
	public static final String FUNC_SPACE_KOREAN = "key_space_korean";
	public static final String FUNC_SHOW_SETTINGS = "key_show_settings";
	public static final String FUNC_UNDO = "key_undo";
	public static final String FUNC_REDO = "key_redo";
	public static final String FUNC_VOICE_INPUT = "key_voice_input";

	public static final String[] FUNCTIONS = {
		FUNC_ADD_WORD,
		FUNC_BACKSPACE,
		FUNC_COMMAND_PALETTE,
		FUNC_EDIT_TEXT,
		FUNC_FILTER_CLEAR,
		FUNC_FILTER_SUGGESTIONS,
		FUNC_PREVIOUS_SUGGESTION,
		FUNC_NEXT_SUGGESTION,
		FUNC_NEXT_INPUT_MODE,
		FUNC_NEXT_LANGUAGE,
		FUNC_SELECT_KEYBOARD,
		FUNC_SHIFT,
		FUNC_SPACE_KOREAN,
		FUNC_SHOW_SETTINGS,
		FUNC_UNDO,
		FUNC_REDO,
		FUNC_VOICE_INPUT,
	};


	SettingsHotkeys(Context context) { super(context); }


	public boolean areHotkeysInitialized() {
		return !prefs.getBoolean(HOTKEY_VERSION, false);
	}


	/**
	 * Applies the default hotkey scheme.
	 * When a standard "Backspace" hardware key is available, "Backspace" hotkey association is not necessary,
	 * so it will be left out blank, to allow the hardware key do its job.
	 * When the on-screen keyboard is on, "Back" is also not associated, because it will cause weird user
	 * experience. Instead the on-screen "Backspace" key can be used.
	 * Arrow keys for manipulating suggestions are also assigned only if available.
	 */
	public void setDefaultKeys() {
		// no default keys
		String[] unassigned = { FUNC_ADD_WORD, FUNC_EDIT_TEXT, FUNC_SELECT_KEYBOARD, FUNC_SHOW_SETTINGS, FUNC_UNDO, FUNC_REDO, FUNC_VOICE_INPUT };
		for (String key : unassigned) {
			prefsEditor.putString(key, String.valueOf(KeyEvent.KEYCODE_UNKNOWN));
		}

		// backspace
		if (
			KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_CLEAR)
			|| KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DEL)
			|| isMainLayoutNumpad()
		) {
			prefsEditor.putString(FUNC_BACKSPACE, String.valueOf(KeyEvent.KEYCODE_UNKNOWN));
		} else {
			prefsEditor.putString(FUNC_BACKSPACE, String.valueOf(KeyEvent.KEYCODE_BACK));
		}

		// filter clear
		prefsEditor.putString(
			FUNC_FILTER_CLEAR,
			String.valueOf(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_DOWN) ? KeyEvent.KEYCODE_DPAD_DOWN : KeyEvent.KEYCODE_UNKNOWN)
		);

		// filter
		prefsEditor.putString(
			FUNC_FILTER_SUGGESTIONS,
			String.valueOf(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_UP) ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_UNKNOWN)
		);

		// previous suggestion
		prefsEditor.putString(
			FUNC_PREVIOUS_SUGGESTION,
			String.valueOf(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_LEFT) ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_UNKNOWN)
		);

		// next suggestion
		prefsEditor.putString(
			FUNC_NEXT_SUGGESTION,
			String.valueOf(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_RIGHT) ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_UNKNOWN)
		);

		prefsEditor.putString(FUNC_COMMAND_PALETTE, String.valueOf(-KeyEvent.KEYCODE_STAR)); // negative means "hold"
		prefsEditor.putString(FUNC_NEXT_INPUT_MODE, String.valueOf(KeyEvent.KEYCODE_POUND));
		prefsEditor.putString(FUNC_NEXT_LANGUAGE, String.valueOf(-KeyEvent.KEYCODE_POUND)); // negative means "hold"
		prefsEditor.putString(FUNC_SHIFT, String.valueOf(KeyEvent.KEYCODE_STAR));
		prefsEditor.putString(
			FUNC_SPACE_KOREAN,
			String.valueOf(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_SPACE) ? KeyEvent.KEYCODE_SPACE : KeyEvent.KEYCODE_STAR)
		);

		prefsEditor.putBoolean(HOTKEY_VERSION, true).apply();
	}


	public int getFunctionKey(String functionName) {
		return getStringifiedInt(functionName, KeyEvent.KEYCODE_UNKNOWN);
	}


	public void setFunctionKey(String functionName, int keyCode) {
		if (isValidFunction(functionName)) {
			Logger.d(LOG_TAG, "Setting hotkey for function: '" + functionName + "' to " + keyCode);
			prefsEditor.putString(functionName, String.valueOf(keyCode)).apply();
		} else {
			Logger.w(LOG_TAG,"Not setting a hotkey for invalid function: '" + functionName + "'");
		}
	}


	public int getKeyAddWord() {
		return getFunctionKey(FUNC_ADD_WORD);
	}
	public int getKeyBackspace() {
		return getFunctionKey(FUNC_BACKSPACE);
	}
	public int getKeyCommandPalette() {
		return getFunctionKey(FUNC_COMMAND_PALETTE);
	}
	public int getKeyEditText() {
		return getFunctionKey(FUNC_EDIT_TEXT);
	}
	public int getKeyFilterClear() {
		return getFunctionKey(FUNC_FILTER_CLEAR);
	}
	public int getKeyFilterSuggestions() {
		return getFunctionKey(FUNC_FILTER_SUGGESTIONS);
	}
	public int getKeyPreviousSuggestion() {
		return getFunctionKey(FUNC_PREVIOUS_SUGGESTION);
	}
	public int getKeyNextSuggestion() {
		return getFunctionKey(FUNC_NEXT_SUGGESTION);
	}
	public int getKeyNextInputMode() {
		return getFunctionKey(FUNC_NEXT_INPUT_MODE);
	}
	public int getKeyNextLanguage() {
		return getFunctionKey(FUNC_NEXT_LANGUAGE);
	}
	public int getKeySelectKeyboard() {
		return getFunctionKey(FUNC_SELECT_KEYBOARD);
	}
	public int getKeyShift() {
		return getFunctionKey(FUNC_SHIFT);
	}
	public int getKeySpaceKorean() {
		return getFunctionKey(FUNC_SPACE_KOREAN);
	}
	public int getKeyShowSettings() {
		return getFunctionKey(FUNC_SHOW_SETTINGS);
	}
	public int getKeyUndo() {
		return getFunctionKey(FUNC_UNDO);
	}
	public int getKeyRedo() {
		return getFunctionKey(FUNC_REDO);
	}
	public int getKeyVoiceInput() {
		return getFunctionKey(FUNC_VOICE_INPUT);
	}


	public String getFunction(int keyCode) {
		if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
			return null;
		}

		for (String key : FUNCTIONS) {
			if (keyCode == getFunctionKey(key)) {
				return key;
			}
		}

		return null;
	}


	private boolean isValidFunction(String functionName) {
		for (String validName : FUNCTIONS) {
			if (validName.equals(functionName)) {
				return true;
			}
		}
		return false;
	}
}
