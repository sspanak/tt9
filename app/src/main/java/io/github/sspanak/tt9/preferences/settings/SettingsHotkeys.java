package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import io.github.sspanak.tt9.commands.CmdAddWord;
import io.github.sspanak.tt9.commands.CmdBackspace;
import io.github.sspanak.tt9.commands.CmdCommandPalette;
import io.github.sspanak.tt9.commands.CmdEditText;
import io.github.sspanak.tt9.commands.CmdEditWord;
import io.github.sspanak.tt9.commands.CmdFilterClear;
import io.github.sspanak.tt9.commands.CmdFilterSuggestions;
import io.github.sspanak.tt9.commands.CmdNextInputMode;
import io.github.sspanak.tt9.commands.CmdNextLanguage;
import io.github.sspanak.tt9.commands.CmdRedo;
import io.github.sspanak.tt9.commands.CmdSelectKeyboard;
import io.github.sspanak.tt9.commands.CmdShift;
import io.github.sspanak.tt9.commands.CmdShowSettings;
import io.github.sspanak.tt9.commands.CmdSpaceKorean;
import io.github.sspanak.tt9.commands.CmdSuggestionNext;
import io.github.sspanak.tt9.commands.CmdSuggestionPrevious;
import io.github.sspanak.tt9.commands.CmdUndo;
import io.github.sspanak.tt9.commands.CmdVoiceInput;
import io.github.sspanak.tt9.commands.Command;
import io.github.sspanak.tt9.commands.CommandCollection;
import io.github.sspanak.tt9.util.Logger;

public class SettingsHotkeys extends SettingsVirtualNumpad {
	private static final String HOTKEY_VERSION = "hotkeys_v6";


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
		String[] unassigned = {CmdAddWord.ID, CmdEditText.ID, CmdSelectKeyboard.ID, CmdShowSettings.ID, CmdUndo.ID, CmdRedo.ID, CmdVoiceInput.ID};
		for (String key : unassigned) {
			getPrefsEditor().putString(key, String.valueOf(KeyEvent.KEYCODE_UNKNOWN));
		}

		// backspace
		if (
			KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_CLEAR)
			|| KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DEL)
			|| isMainLayoutLarge()
		) {
			getPrefsEditor().putString(CmdBackspace.ID, String.valueOf(KeyEvent.KEYCODE_UNKNOWN));
		} else {
			getPrefsEditor().putString(CmdBackspace.ID, String.valueOf(KeyEvent.KEYCODE_BACK));
		}

		// filter clear
		getPrefsEditor().putString(
			CmdFilterClear.ID,
			String.valueOf(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_DOWN) ? KeyEvent.KEYCODE_DPAD_DOWN : KeyEvent.KEYCODE_UNKNOWN)
		);

		// filter
		getPrefsEditor().putString(
			CmdFilterSuggestions.ID,
			String.valueOf(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_UP) ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_UNKNOWN)
		);

		// previous suggestion
		getPrefsEditor().putString(
			CmdSuggestionPrevious.ID,
			String.valueOf(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_LEFT) ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_UNKNOWN)
		);

		// next suggestion
		getPrefsEditor().putString(
			CmdSuggestionNext.ID,
			String.valueOf(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_RIGHT) ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_UNKNOWN)
		);

		getPrefsEditor().putString(CmdCommandPalette.ID, String.valueOf(-KeyEvent.KEYCODE_STAR)); // negative means "hold"
		getPrefsEditor().putString(CmdNextInputMode.ID, String.valueOf(KeyEvent.KEYCODE_POUND));
		getPrefsEditor().putString(CmdNextLanguage.ID, String.valueOf(-KeyEvent.KEYCODE_POUND)); // negative means "hold"
		getPrefsEditor().putString(CmdShift.ID, String.valueOf(KeyEvent.KEYCODE_STAR));
		getPrefsEditor().putString(
			CmdSpaceKorean.ID,
			String.valueOf(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_SPACE) ? KeyEvent.KEYCODE_SPACE : KeyEvent.KEYCODE_STAR)
		);

		getPrefsEditor().putBoolean(HOTKEY_VERSION, true).apply();
	}


	public int getFunctionKey(String functionName) {
		return getStringifiedInt(functionName, KeyEvent.KEYCODE_UNKNOWN);
	}


	public void setFunctionKey(String functionName, int keyCode) {
		if (isValidFunction(functionName)) {
			Logger.d(LOG_TAG, "Setting hotkey for function: '" + functionName + "' to " + keyCode);
			getPrefsEditor().putString(functionName, String.valueOf(keyCode)).apply();
		} else {
			Logger.w(LOG_TAG,"Not setting a hotkey for invalid function: '" + functionName + "'");
		}
	}


	public int getKeyAddWord() {
		return getFunctionKey(CmdAddWord.ID);
	}
	public int getKeyBackspace() {
		return getFunctionKey(CmdBackspace.ID);
	}
	public int getKeyCommandPalette() {
		return getFunctionKey(CmdCommandPalette.ID);
	}
	public int getKeyEditText() {
		return getFunctionKey(CmdEditText.ID);
	}
	public int getKeyEditWord() {
		return getFunctionKey(CmdEditWord.ID);
	}
	public int getKeyFilterClear() {
		return getFunctionKey(CmdFilterClear.ID);
	}
	public int getKeyFilterSuggestions() {
		return getFunctionKey(CmdFilterSuggestions.ID);
	}
	public int getKeyPreviousSuggestion() {
		return getFunctionKey(CmdSuggestionPrevious.ID);
	}
	public int getKeyNextSuggestion() {
		return getFunctionKey(CmdSuggestionNext.ID);
	}
	public int getKeyNextInputMode() {
		return getFunctionKey(CmdNextInputMode.ID);
	}
	public int getKeyNextLanguage() {
		return getFunctionKey(CmdNextLanguage.ID);
	}
	public int getKeySelectKeyboard() {
		return getFunctionKey(CmdSelectKeyboard.ID);
	}
	public int getKeyShift() {
		return getFunctionKey(CmdShift.ID);
	}
	public int getKeySpaceKorean() {
		return getFunctionKey(CmdSpaceKorean.ID);
	}
	public int getKeyShowSettings() {
		return getFunctionKey(CmdShowSettings.ID);
	}
	public int getKeyUndo() {
		return getFunctionKey(CmdUndo.ID);
	}
	public int getKeyRedo() {
		return getFunctionKey(CmdRedo.ID);
	}
	public int getKeyVoiceInput() {
		return getFunctionKey(CmdVoiceInput.ID);
	}


	public String getFunction(int keyCode) {
		if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
			return null;
		}

		for (Command cmd : CommandCollection.getHotkeyCommands()) {
			if (keyCode == getFunctionKey(cmd.getId())) {
				return cmd.getId();
			}
		}

		return null;
	}


	private boolean isValidFunction(String functionName) {
		for (Command cmd : CommandCollection.getHotkeyCommands()) {
			if (cmd.getId().equals(functionName)) {
				return true;
			}
		}
		return false;
	}
}
