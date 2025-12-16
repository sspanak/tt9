package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.view.Gravity;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownAlignment;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownBottomPaddingPortrait;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownLayoutType;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownSettingsFontSize;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownSuggestionFontSize;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownWidth;
import io.github.sspanak.tt9.preferences.screens.appearance.SwitchKeyShadows;
import io.github.sspanak.tt9.preferences.screens.languages.AddWordsWithoutConfirmationSwitch;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class SettingsUI extends SettingsTyping {
	public final static int FONT_SIZE_DEFAULT = 0;
	public final static int FONT_SIZE_LARGE = 2;

	public final static float KEY_SHADOW_ELEVATION = 3f;
	public final static float KEY_SHADOW_TRANSLATION = 2f;

	public final static int LAYOUT_STEALTH = 0;
	public final static int LAYOUT_TRAY = 2;
	public final static int LAYOUT_SMALL = 3;
	public final static int LAYOUT_NUMPAD = 4;
	public final static int LAYOUT_CLASSIC = 5;

	private final int DEFAULT_LAYOUT;
	private final int DEFAULT_LARGE_LAYOUT;

	public final static int MIN_WIDTH_PERCENT = 50;
	private int DEFAULT_WIDTH_LANDSCAPE = 0;
	private Boolean DEFAULT_QUICK_SWITCH_LANGUAGE = null;


	SettingsUI(Context context) {
		super(context);

		DEFAULT_LARGE_LAYOUT = LAYOUT_CLASSIC;

		if (DeviceInfo.noKeyboard(context)) {
			DEFAULT_LAYOUT = DEFAULT_LARGE_LAYOUT;
		} else if (DeviceInfo.noBackspaceKey() && !DeviceInfo.noTouchScreen(context)) {
			DEFAULT_LAYOUT = LAYOUT_SMALL;
		} else {
			DEFAULT_LAYOUT = LAYOUT_TRAY;
		}
	}

	public boolean getAddWordsNoConfirmation() {
		return prefs.getBoolean(AddWordsWithoutConfirmationSwitch.NAME, false);
	}

	public boolean getNotificationsApproved() {
		return !DeviceInfo.AT_LEAST_ANDROID_13 || getStringifiedInt("pref_asked_for_notifications_version", 0) == Integer.MAX_VALUE;
	}

	public boolean shouldAskForNotifications() {
		return DeviceInfo.AT_LEAST_ANDROID_13 && getStringifiedInt("pref_asked_for_notifications_version", 0) < BuildConfig.VERSION_CODE;
	}

	public int getBottomPaddingPortrait() {
		return getStringifiedInt(DropDownBottomPaddingPortrait.NAME, DropDownBottomPaddingPortrait.DEFAULT);
	}

	public int getBottomPaddingPortraitPx() {
		return Math.round(getBottomPaddingPortrait() * DeviceInfo.getScreenPixelDensity(context));
	}

	/**
	 * Samsung devices with Android 15+ SOMETIMES report bottom inset = navigational bar height, but
	 * but they still move up the IME window up, the Android 14 way. So, if we apply our bottom padding,
	 * we end up with double padding. To avoid this, we read the reported device bottom inset and
	 * overwrite the default bottom padding accordingly.
	 * Safe to call on non-Samsung devices and pre-Android 15 devices. It will just do nothing.
	 */
	public void setSamsungBottomPaddingPortrait(int paddingDp) {
		if (
			DeviceInfo.IS_SAMSUNG
			&& DeviceInfo.AT_LEAST_ANDROID_15
			&& paddingDp > 0
			&& getStringifiedInt(DropDownBottomPaddingPortrait.NAME, -1) == -1
		) {
			getPrefsEditor().putString(DropDownBottomPaddingPortrait.NAME, Integer.toString(paddingDp)).apply();
		}
	}

	public void setNotificationsApproved(boolean yes) {
		getPrefsEditor().putString(
			"pref_asked_for_notifications_version",
			Integer.toString(yes ? Integer.MAX_VALUE : BuildConfig.VERSION_CODE)
		);
		getPrefsEditor().apply();
	}

	public boolean isStatusIconEnabled() {
		return prefs.getBoolean("pref_status_icon", DeviceInfo.IS_QIN_F21 || !DeviceInfo.noKeyboard(context));
	}

	public boolean getDragResize() {
		return prefs.getBoolean("pref_drag_resize", true);
	}

	public boolean getDoubleTapResize() {
		return prefs.getBoolean("pref_double_tap_resize", false);
	}

	public boolean getHapticFeedback() {
		return prefs.getBoolean("pref_haptic_feedback", true);
	}

	public int getAlignment() {
		return getStringifiedInt(DropDownAlignment.NAME, Gravity.CENTER_HORIZONTAL);
	}

	public void setAlignment(int alignment) {
		if (alignment != Gravity.CENTER_HORIZONTAL && alignment != Gravity.START && alignment != Gravity.END) {
			Logger.w(getClass().getSimpleName(), "Ignoring invalid numpad key alignment: " + alignment);
		}

		getPrefsEditor().putString(DropDownAlignment.NAME, Integer.toString(alignment));
		getPrefsEditor().apply();
	}

	public boolean getQuickSwitchLanguage() {
		if (DEFAULT_QUICK_SWITCH_LANGUAGE == null) {
			DEFAULT_QUICK_SWITCH_LANGUAGE = !isMainLayoutStealth() && !areEnabledLanguagesMoreThanN(2);
		}

		return prefs.getBoolean("pref_quick_switch_language", DEFAULT_QUICK_SWITCH_LANGUAGE);
	}

	public boolean getKeyShadows() {
		return prefs.getBoolean(SwitchKeyShadows.NAME, SwitchKeyShadows.DEFAULT);
	}

	public int getSettingsFontSize() {
		int defaultSize = DeviceInfo.IS_QIN_F21 || DeviceInfo.IS_LG_X100S ? FONT_SIZE_LARGE : FONT_SIZE_DEFAULT;
		return getStringifiedInt(DropDownSettingsFontSize.NAME, defaultSize);
	}

	public float getSuggestionFontScale() {
		return getSuggestionFontSizePercent() / 100f;
	}

	public int getSuggestionFontSizePercent() {
		return getStringifiedInt(DropDownSuggestionFontSize.NAME, 100);
	}

	public boolean getSuggestionSmoothScroll() {
		return prefs.getBoolean("pref_suggestion_smooth_scroll", !DeviceInfo.noTouchScreen(context));
	}

	public int getDefaultWidthPercent(boolean isPortrait) {
		if (isPortrait) {
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

	public int getWidthPercent(boolean isPortrait) {
		return getStringifiedInt(DropDownWidth.NAME, getDefaultWidthPercent(isPortrait));
	}

	public void setMainViewLayout(int layout) {
		if (layout != LAYOUT_STEALTH && layout != LAYOUT_TRAY && layout != LAYOUT_SMALL && layout != LAYOUT_NUMPAD && layout != LAYOUT_CLASSIC) {
			Logger.w(getClass().getSimpleName(), "Ignoring invalid main view layout: " + layout);
			return;
		}

		getPrefsEditor().putString(DropDownLayoutType.NAME, Integer.toString(layout));
		getPrefsEditor().apply();
	}

	public int getMainViewLayout() {
		return getStringifiedInt(DropDownLayoutType.NAME, DEFAULT_LAYOUT);
	}

	public int getPreferredLargeLayout() {
		final int layout = prefs.getInt("pref_preferred_large_layout", DEFAULT_LARGE_LAYOUT);
		return layout != LAYOUT_CLASSIC && layout != LAYOUT_NUMPAD ? DEFAULT_LARGE_LAYOUT : layout;
	}

	public void setPreferredLargeLayout(int layout) {
		if (layout != LAYOUT_CLASSIC && layout != LAYOUT_NUMPAD) {
			Logger.w(getClass().getSimpleName(), "Ignoring invalid preferred large layout: " + layout);
			return;
		}

		getPrefsEditor().putInt("pref_preferred_large_layout", layout).apply();
	}

	public boolean isMainLayoutLarge() {
		final int layout = getMainViewLayout();
		return layout == LAYOUT_CLASSIC || layout == LAYOUT_NUMPAD;
	}

	public boolean isMainLayoutClassic() { return getMainViewLayout() == LAYOUT_CLASSIC; }
	public boolean isMainLayoutNumpad() { return getMainViewLayout() == LAYOUT_NUMPAD; }
	public boolean isMainLayoutTray() { return getMainViewLayout() == LAYOUT_TRAY; }
	public boolean isMainLayoutSmall() { return getMainViewLayout() == LAYOUT_SMALL; }
	public boolean isMainLayoutStealth() { return getMainViewLayout() == LAYOUT_STEALTH; }
}
