package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatDelegate;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class SettingsUI extends SettingsTyping {
	public final static int FONT_SIZE_DEFAULT = 0;
	public final static int FONT_SIZE_LARGE = 2;

	public final static int LAYOUT_STEALTH = 0;
	public final static int LAYOUT_TRAY = 2;
	public final static int LAYOUT_SMALL = 3;
	public final static int LAYOUT_NUMPAD = 4;

	private final int DEFAULT_LAYOUT;

	public final static int MIN_WIDTH_PERCENT = 50;
	private int DEFAULT_WIDTH_LANDSCAPE = 0;
	private Boolean DEFAULT_QUICK_SWITCH_LANGUAGE = null;


	SettingsUI(Context context) {
		super(context);

		if (DeviceInfo.noKeyboard(context)) {
			DEFAULT_LAYOUT = LAYOUT_NUMPAD;
		} else if (DeviceInfo.noBackspaceKey() && !DeviceInfo.noTouchScreen(context)) {
			DEFAULT_LAYOUT = LAYOUT_SMALL;
		} else {
			DEFAULT_LAYOUT = LAYOUT_TRAY;
		}
	}

	public boolean getAddWordsNoConfirmation() {
		return prefs.getBoolean("add_word_no_confirmation", false);
	}

	public boolean getNotificationsApproved() {
		return !DeviceInfo.AT_LEAST_ANDROID_13 || getStringifiedInt("pref_asked_for_notifications_version", 0) == Integer.MAX_VALUE;
	}

	public boolean shouldAskForNotifications() {
		return DeviceInfo.AT_LEAST_ANDROID_13 && getStringifiedInt("pref_asked_for_notifications_version", 0) < BuildConfig.VERSION_CODE;
	}

	public void setNotificationsApproved(boolean yes) {
		prefsEditor.putString(
			"pref_asked_for_notifications_version",
			Integer.toString(yes ? Integer.MAX_VALUE : BuildConfig.VERSION_CODE)
		);
		prefsEditor.apply();
	}

	public boolean isStatusIconEnabled() {
		return prefs.getBoolean("pref_status_icon", DeviceInfo.IS_QIN_F21 || !DeviceInfo.noKeyboard(context));
	}

	public boolean getDarkTheme() {
		int theme = getTheme();
		if (theme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
			return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
		} else {
			return theme == AppCompatDelegate.MODE_NIGHT_YES;
		}
	}

	public boolean getDragResize() {
		return prefs.getBoolean("pref_drag_resize", true);
	}

	public boolean getHapticFeedback() {
		return prefs.getBoolean("pref_haptic_feedback", true);
	}

	public int getAlignment() {
		return getStringifiedInt("pref_numpad_alignment", Gravity.CENTER_HORIZONTAL);
	}

	public void setAlignment(int alignment) {
		if (alignment != Gravity.CENTER_HORIZONTAL && alignment != Gravity.START && alignment != Gravity.END) {
			Logger.w(getClass().getSimpleName(), "Ignoring invalid numpad key alignment: " + alignment);
		}

		prefsEditor.putString("pref_numpad_alignment", Integer.toString(alignment));
		prefsEditor.apply();
	}

	public boolean getQuickSwitchLanguage() {
		if (DEFAULT_QUICK_SWITCH_LANGUAGE == null) {
			DEFAULT_QUICK_SWITCH_LANGUAGE = !isMainLayoutStealth() && getEnabledLanguagesIdsAsStrings().size() <= 2;
		}

		return prefs.getBoolean("pref_quick_switch_language", DEFAULT_QUICK_SWITCH_LANGUAGE);
	}

	public int getSettingsFontSize() {
		int defaultSize = DeviceInfo.IS_QIN_F21 || DeviceInfo.IS_LG_X100S ? FONT_SIZE_LARGE : FONT_SIZE_DEFAULT;
		return getStringifiedInt("pref_font_size", defaultSize);
	}

	public float getSuggestionFontScale() {
		return getSuggestionFontSizePercent() / 100f;
	}

	public int getSuggestionFontSizePercent() {
		return getStringifiedInt("pref_suggestion_font_size", 100);
	}

	public boolean getSuggestionSmoothScroll() {
		return prefs.getBoolean("pref_suggestion_smooth_scroll", !DeviceInfo.noTouchScreen(context));
	}

	public int getTheme() {
		return getStringifiedInt("pref_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
	}

	public int getDefaultWidthPercent() {
		if (!DeviceInfo.isLandscapeOrientation(context)) {
			return 100;
		}

		if (DEFAULT_WIDTH_LANDSCAPE > 0) {
			return DEFAULT_WIDTH_LANDSCAPE;
		}

		int screenWidth = DeviceInfo.getScreenWidth(context.getApplicationContext());
		if (screenWidth < 1) {
			return 100;
		}

		int stylesMaxWidth = Math.round(context.getResources().getDimension(R.dimen.numpad_max_width));
		float width = screenWidth < stylesMaxWidth ? 100 : 100f * stylesMaxWidth / screenWidth;
		width = width < MIN_WIDTH_PERCENT ? MIN_WIDTH_PERCENT : width;

		return DEFAULT_WIDTH_LANDSCAPE = Math.round(width / 5) * 5;
	}

	public int getWidthPercent() {
		return getStringifiedInt("pref_numpad_width", getDefaultWidthPercent());
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
		return getStringifiedInt("pref_layout_type", DEFAULT_LAYOUT);
	}

	public boolean isMainLayoutNumpad() { return getMainViewLayout() == LAYOUT_NUMPAD; }
	public boolean isMainLayoutTray() { return getMainViewLayout() == LAYOUT_TRAY; }
	public boolean isMainLayoutSmall() { return getMainViewLayout() == LAYOUT_SMALL; }
	public boolean isMainLayoutStealth() { return getMainViewLayout() == LAYOUT_STEALTH; }
}
