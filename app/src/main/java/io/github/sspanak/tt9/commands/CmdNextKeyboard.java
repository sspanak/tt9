package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.DeviceInfo;
import io.github.sspanak.tt9.util.sys.SystemSettings;

public class CmdNextKeyboard implements Command {
	public static final String ID = "key_next_keyboard";
	public String getId() { return ID; }
	public int getIcon() { return -1; }
	public int getName() { return 0; }

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}

		tt9.getSuggestionOps().cancelDelayedAccept();
		tt9.stopVoiceInput();

		if (DeviceInfo.AT_LEAST_ANDROID_9) {
			tt9.switchToPreviousInputMethod();
			return true;
		}

		try {
			tt9.switchInputMethod(SystemSettings.getPreviousIME(tt9));
		} catch (Exception e) {
			Logger.d(getClass().getSimpleName(), "Could not switch to previous input method. " + e);
		}

		return true;
	}
}
