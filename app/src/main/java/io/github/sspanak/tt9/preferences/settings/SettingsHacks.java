package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import io.github.sspanak.tt9.preferences.screens.debug.ItemInputHandlingMode;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

class SettingsHacks extends BaseSettings {
	private boolean demoMode = false;

	SettingsHacks(Context context) { super(context); }

	/************* debugging settings *************/

	public boolean getDemoMode() {
		return demoMode;
	}

	public void setDemoMode(boolean demoMode) {
		this.demoMode = demoMode;
	}

	public int getLogLevel() {
		return getStringifiedInt("pref_log_level", Logger.LEVEL);
	}

	public boolean getEnableSystemLogs() {
		return prefs.getBoolean("pref_enable_system_logs", false);
	}

	public int getInputHandlingMode() {
		return getStringifiedInt("pref_input_handling_mode", ItemInputHandlingMode.NORMAL);
	}


	/************* hack settings *************/

	public int getSuggestionScrollingDelay() {
		boolean defaultOn = DeviceInfo.noTouchScreen(context) && !DeviceInfo.AT_LEAST_ANDROID_10;
		return prefs.getBoolean("pref_alternative_suggestion_scrolling", defaultOn) ? 200 : 0;
	}

	public boolean clearInsets() {
		return prefs.getBoolean("pref_clear_insets", DeviceInfo.isSonimGen2(context));
	}

	/**
	 * Protection against faulty devices, that sometimes send two (or more) click events
	 * per a single key press, which absolutely undesirable side effects.
	 * There were reports about this on <a href="https://github.com/sspanak/tt9/issues/117">Kyocera KYF31</a>
	 * and on <a href="https://github.com/sspanak/tt9/issues/399">CAT S22</a>.
	 */
	public int getKeyPadDebounceTime() {
		int defaultTime = DeviceInfo.IS_CAT_S22_FLIP ? 50 : 0;
		defaultTime = DeviceInfo.IS_QIN_F21 ? 20 : defaultTime;
		return getStringifiedInt("pref_key_pad_debounce_time", defaultTime);
	}

	public boolean getSystemLogs() {
		return prefs.getBoolean("pref_enable_system_logs", false);
	}

	public boolean getDonationsVisible() {
		return prefs.getBoolean("pref_show_donations", false);
	}

	public void setDonationsVisible(boolean yes) {
		prefsEditor.putBoolean("pref_show_donations", yes).apply();
	}

	public boolean getAllowComposingText() {
		return prefs.getBoolean("pref_allow_composing_text", true);
	}

	/**
	 * On Samsung S25 (SM-S931B), edge-to-edge does not work like on Pixel/Xiaomi/etc. Like on Android 14,
	 * the navigation bar is subtracted from the initial available screen size, so we must not add padding
	 * to compensate.
	 * There has been a report that Samsung S24U also behaves like this after upgrading to Android 15.
	 * @see <a href="https://github.com/sspanak/tt9/issues/755">extra space at the bottom of the layout</a>
	 */
	public boolean getPrecalculateNavbarHeight() {
		return prefs.getBoolean("hack_precalculate_navbar_height_v3", !DeviceInfo.IS_SAMSUNG);
	}


	/**
	 * Facebook Messenger has a bug where when trying to reply to a message, and when the keyboard
	 * has certain height, it somehow switches the focus outside of the text field. The problematic
	 * height is exactly the height when the Main View is Small or when the Command Palette is shown.
	 * With this hack, we tell the Main View to become taller and mitigate the issue.
	 * More info: <a href="https://github.com/sspanak/tt9/issues/815">Issue 815</a>. Note that the
	 * bug happens on every phone, not only on Freetel.
	 */
	public boolean getMessengerReplyExtraPadding() {
		return prefs.getBoolean("hack_messenger_reply_extra_padding", false);
	}

	public void setMessengerReplyExtraPadding(boolean enabled) {
		prefsEditor.putBoolean("hack_messenger_reply_extra_padding", enabled).apply();
	}
}
