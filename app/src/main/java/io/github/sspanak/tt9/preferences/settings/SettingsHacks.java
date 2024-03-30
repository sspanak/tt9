package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import io.github.sspanak.tt9.preferences.screens.debug.ItemInputHandlingMode;
import io.github.sspanak.tt9.util.Logger;

class SettingsHacks extends BaseSettings {
	SettingsHacks(Context context) { super(context); }

	/************* debugging settings *************/

	public int getLogLevel() {
		try {
			return Integer.parseInt(prefs.getString("pref_log_level", String.valueOf(Logger.LEVEL)));
		} catch (NumberFormatException ignored) {
			return Logger.LEVEL;
		}
	}

	public int getInputHandlingMode() {
		try {
			return Integer.parseInt(prefs.getString("pref_input_handling_mode", String.valueOf(ItemInputHandlingMode.NORMAL)));
		} catch (NumberFormatException ignored) {
			return ItemInputHandlingMode.NORMAL;
		}
	}


	/************* hack settings *************/

	public int getSuggestionScrollingDelay() {
		return prefs.getBoolean("pref_alternative_suggestion_scrolling", false) ? 200 : 0;
	}

	public boolean getFbMessengerHack() {
		return prefs.getBoolean("pref_hack_fb_messenger", false);
	}

	public boolean getGoogleChatHack() {
		return prefs.getBoolean("pref_hack_google_chat", false);
	}

	/**
	 * Protection against faulty devices, that sometimes send two (or more) click events
	 * per a single key press, which absolutely undesirable side effects.
	 * There were reports about this on <a href="https://github.com/sspanak/tt9/issues/117">Kyocera KYF31</a>
	 * and on <a href="https://github.com/sspanak/tt9/issues/399">CAT S22</a>.
	 */

	public int getKeyPadDebounceTime() {
		try {
			return Integer.parseInt(prefs.getString("pref_key_pad_debounce_time", "0"));
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
