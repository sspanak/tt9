package io.github.sspanak.tt9.ime.helpers;

import android.content.res.Resources;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import io.github.sspanak.tt9.R;
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

public class Hotkey {
	private final Resources resources;
	private final HashMap<String, Integer> defaults;

	public Hotkey(@NonNull Resources resources) {
		this.resources = resources;

		defaults = new HashMap<>();
		defaults.put(CmdAddWord.ID, R.integer.hotkey_add_word);
		defaults.put(CmdBackspace.ID, R.integer.hotkey_backspace);
		defaults.put(CmdCommandPalette.ID, R.integer.hotkey_command_palette);
		defaults.put(CmdEditText.ID, R.integer.hotkey_edit_text);
		defaults.put(CmdEditWord.ID, R.integer.hotkey_edit_word);
		defaults.put(CmdFilterClear.ID, R.integer.hotkey_filter_clear);
		defaults.put(CmdFilterSuggestions.ID, R.integer.hotkey_filter_suggestions);
		defaults.put(CmdNextInputMode.ID, R.integer.hotkey_next_input_mode);
		defaults.put(CmdNextLanguage.ID, R.integer.hotkey_next_language);
		defaults.put(CmdSelectKeyboard.ID, R.integer.hotkey_select_keyboard);
		defaults.put(CmdShift.ID, R.integer.hotkey_shift);
		defaults.put(CmdShowSettings.ID, R.integer.hotkey_show_settings);
		defaults.put(CmdSpaceKorean.ID, R.integer.hotkey_space_korean);
		defaults.put(CmdSuggestionNext.ID, R.integer.hotkey_next_suggestion);
		defaults.put(CmdSuggestionPrevious.ID, R.integer.hotkey_previous_suggestion);
		defaults.put(CmdUndo.ID, R.integer.hotkey_undo);
		defaults.put(CmdRedo.ID, R.integer.hotkey_redo);
		defaults.put(CmdVoiceInput.ID, R.integer.hotkey_voice_input);
	}

	public Set<String> getAllKeys() {
		return new HashSet<>(defaults.keySet());
	}

	public String getCode(@Nullable String key) {
		int keyCode = getCodeByResId(defaults.getOrDefault(key, 0));

		if (CmdSpaceKorean.ID.equals(key) && keyCode == KeyEvent.KEYCODE_UNKNOWN) {
			keyCode = getCodeByResId(R.integer.hotkey_space_korean_fallback);
		}

		return String.valueOf(keyCode);
	}

	private int getCodeByResId(@Nullable Integer resId) {
		if (resId == null || resId == 0) {
			return KeyEvent.KEYCODE_UNKNOWN;
		}

		try {
			int keyCode = resources.getInteger(resId);
			return KeyCharacterMap.deviceHasKey(Math.abs(keyCode)) ? keyCode : KeyEvent.KEYCODE_UNKNOWN;
		} catch (Resources.NotFoundException e) {
			return KeyEvent.KEYCODE_UNKNOWN;
		}
	}
}
