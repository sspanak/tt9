package io.github.sspanak.tt9.commands;

import android.view.KeyEvent;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class CmdBackspace implements Command {
	public static final String ID = "key_backspace";
	public String getId() { return ID; }
	public int getIcon() { return 0; }
	public String getIconTxt(boolean rtl) { return rtl ? "⌦" : "⌫"; }
	public int getName() { return io.github.sspanak.tt9.R.string.function_backspace; }

	public boolean run(@Nullable TraditionalT9 tt9) {
		deleteText(tt9, 1);
		return true;
	}

	public static void deleteText(@Nullable TraditionalT9 tt9, int repeatCount) {
		if (tt9 != null && !tt9.onBackspace(repeatCount)) {
			// Limited or special numeric field (e.g. formatted money or dates) cannot always return
			// the text length, therefore onBackspace() seems them as empty and does nothing. This results
			// in fallback to the default hardware key action. Here we simulate the hardware BACKSPACE.
			tt9.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
		}
	}

	public static void deleteWord(@Nullable TraditionalT9 tt9) {
		if (tt9 != null) {
			tt9.onBackspace(SettingsStore.BACKSPACE_ACCELERATION_REPEAT_DEBOUNCE);
		}
	}
}
