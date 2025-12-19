package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.github.sspanak.tt9.commands.CmdAddWord;
import io.github.sspanak.tt9.commands.CmdEditText;
import io.github.sspanak.tt9.commands.CmdRedo;
import io.github.sspanak.tt9.commands.CmdUndo;
import io.github.sspanak.tt9.commands.CmdVoiceInput;
import io.github.sspanak.tt9.commands.NullCommand;

public abstract class SettingsCustomKeyActions extends SettingsUI {
	public static final String CUSTOM_ACTION_KEY_1 = "_1";
	public static final String CUSTOM_ACTION_KEY_2 = "_2";
	public static final String CUSTOM_ACTION_KEY_3 = "_3";
	public static final String CUSTOM_ACTION_KEY_4 = "_4";
	public static final String CUSTOM_ACTION_KEY_5 = "_5";
	public static final String CUSTOM_ACTION_KEY_6 = "_6";
	public static final String CUSTOM_ACTION_KEY_7 = "_7";
	public static final String CUSTOM_ACTION_KEY_8 = "_8";
	public static final String CUSTOM_ACTION_KEY_9 = "_9";

	private static final HashMap<String, String> classicLayoutDefaultsSwipeRight = new HashMap<>() {{
		put(CUSTOM_ACTION_KEY_1, CmdAddWord.ID);
		put(CUSTOM_ACTION_KEY_4, CmdUndo.ID);
	}};

	private static final HashMap<String, String> classicLayoutDefaultsSwipeLeft = new HashMap<>() {{
		put(CUSTOM_ACTION_KEY_3, CmdVoiceInput.ID);
		put(CUSTOM_ACTION_KEY_5, CmdEditText.ID);
		put(CUSTOM_ACTION_KEY_6, CmdRedo.ID);
	}};

	public final int MOVE_CURSOR_WITH_SPACE_THRESHOLD = 0;

	protected SettingsCustomKeyActions(Context context) {
		super(context);
	}

	public boolean getMoveCursorWithSpace() {
		return false;
	}

	@NonNull
	public String getSwipeRightCommand(String keySuffix) {
		if (keySuffix == null || keySuffix.isEmpty() || !classicLayoutDefaultsSwipeRight.containsKey(keySuffix) || !isMainLayoutClassic()) {
			return NullCommand.ID;
		}

		String defaultCmd = classicLayoutDefaultsSwipeRight.get(keySuffix);
		return defaultCmd != null ? defaultCmd : NullCommand.ID;
	}

	@NonNull
	public String getSwipeLeftCommand(String keySuffix) {
		if (keySuffix == null || keySuffix.isEmpty() || !classicLayoutDefaultsSwipeLeft.containsKey(keySuffix) || !isMainLayoutClassic()) {
			return NullCommand.ID;
		}

		String defaultCmd = classicLayoutDefaultsSwipeLeft.get(keySuffix);
		return defaultCmd != null ? defaultCmd : NullCommand.ID;
	}
}
