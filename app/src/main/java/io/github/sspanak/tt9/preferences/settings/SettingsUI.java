package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatDelegate;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.util.Logger;

public class SettingsUI extends SettingsTyping {
	public final static int FONT_SIZE_DEFAULT = 0;
	public final static int FONT_SIZE_LARGE = 2;

	public final static int LAYOUT_STEALTH = 0;
	public final static int LAYOUT_TRAY = 2;
	public final static int LAYOUT_SMALL = 3;
	public final static int LAYOUT_NUMPAD = 4;


	SettingsUI(Context context) { super(context); }

	public boolean getAddWordsNoConfirmation() {
		return prefs.getBoolean("add_word_no_confirmation", false);
	}

	public boolean isStatusIconEnabled() {
		return prefs.getBoolean("pref_status_icon", DeviceInfo.isQinF21());
	}

	public boolean getDarkTheme() {
		int theme = getTheme();
		if (theme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
			return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
		} else {
			return theme == AppCompatDelegate.MODE_NIGHT_YES;
		}
	}

	public boolean getHapticFeedback() {
		return prefs.getBoolean("pref_haptic_feedback", true);
	}

	public int getNumpadAlignment() {
		return getStringifiedInt("pref_numpad_alignment", Gravity.CENTER_HORIZONTAL);
	}

	public void setNumpadAlignment(int alignment) {
		if (alignment != Gravity.CENTER_HORIZONTAL && alignment != Gravity.START && alignment != Gravity.END) {
			Logger.w(getClass().getSimpleName(), "Ignoring invalid numpad key alignment: " + alignment);
		}

		prefsEditor.putString("pref_numpad_alignment", Integer.toString(alignment));
		prefsEditor.apply();
	}

	public int getNumpadKeyDefaultHeight() {
		return context.getResources().getDimensionPixelSize(R.dimen.numpad_key_height);
	}

	public int getNumpadKeyHeight() {
		return getStringifiedInt("pref_numpad_key_height", getNumpadKeyDefaultHeight());
	}

	public int getSettingsFontSize() {
		int defaultSize = DeviceInfo.isQinF21() || DeviceInfo.isLgX100S() ? FONT_SIZE_LARGE : FONT_SIZE_DEFAULT;
		return getStringifiedInt("pref_font_size", defaultSize);
	}

	public int getTheme() {
		return getStringifiedInt("pref_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
	}

	public void setMainViewLayout(int layout) {
		if (layout != LAYOUT_STEALTH && layout != LAYOUT_TRAY && layout != LAYOUT_SMALL && layout != LAYOUT_NUMPAD) {
			Logger.w(getClass().getSimpleName(), "Ignoring invalid main view layout: " + layout);
			return;
		}

		prefsEditor.putString("pref_layout_type", Integer.toString(layout));
		prefsEditor.apply();
	}

	public int getMainViewLayout() {
		int defaultLayout = LAYOUT_SMALL;
		if (DeviceInfo.noTouchScreen(context)) {
			defaultLayout = LAYOUT_TRAY;
		} else if (DeviceInfo.noKeyboard(context)) {
			defaultLayout = LAYOUT_NUMPAD;
		}

		return getStringifiedInt("pref_layout_type", defaultLayout);
	}

	public boolean isMainLayoutNumpad() { return getMainViewLayout() == LAYOUT_NUMPAD; }
	public boolean isMainLayoutTray() { return getMainViewLayout() == LAYOUT_TRAY; }
	public boolean isMainLayoutSmall() { return getMainViewLayout() == LAYOUT_SMALL; }
	public boolean isMainLayoutStealth() { return getMainViewLayout() == LAYOUT_STEALTH; }
}
