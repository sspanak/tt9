package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatDelegate;

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

	public final static int NUMPAD_SHAPE_SQUARE = 0;
	public final static int NUMPAD_SHAPE_V = 1;
	public final static int NUMPAD_SHAPE_LONG_SPACE = 2;

	private final int DEFAULT_LAYOUT;

	public final static int MIN_WIDTH_PERCENT = 50;
	private int DEFAULT_WIDTH_LANDSCAPE = 0;


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

	public boolean areArrowKeysHidden() {
		return !prefs.getBoolean("pref_arrow_keys_visible", true);
	}

	public boolean getAddWordsNoConfirmation() {
		return prefs.getBoolean("add_word_no_confirmation", false);
	}

	public boolean isStatusIconEnabled() {
		return prefs.getBoolean("pref_status_icon", DeviceInfo.IS_QIN_F21);
	}

	public boolean isStatusIconTypeModeEnabled(){
		return prefs.getBoolean("pref_status_icon_input_mode", false);
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

	public int getNumpadKeyDefaultHeight() {
		return context.getResources().getDimensionPixelSize(R.dimen.numpad_key_height);
	}

	public int getNumpadKeyHeight() {
		return getStringifiedInt("pref_numpad_key_height", getNumpadKeyDefaultHeight());
	}

	public float getNumpadFnKeyDefaultScale() {
		// The simpler getResource.getFloat() requires API 29, so we must get the value manually.
		try {
			TypedValue outValue = new TypedValue();
			context.getResources().getValue(R.dimen.numpad_key_fn_layout_weight, outValue, true);
			return outValue.getFloat();
		} catch (Exception e) {
			return 0.625f;
		}
	}

	public float getNumpadFnKeyScale() {
		return getStringifiedFloat("pref_numpad_fn_key_width", getNumpadFnKeyDefaultScale());
	}

	public int getNumpadKeyFontSizePercent() {
		return isMainLayoutNumpad() ? getStringifiedInt("pref_numpad_key_font_size", 100) : 100;
	}

	public int getNumpadShape() {
		return getStringifiedInt("pref_numpad_shape", NUMPAD_SHAPE_SQUARE);
	}

	public boolean isNumpadShapeLongSpace() { return getNumpadShape() == NUMPAD_SHAPE_LONG_SPACE; }
	public boolean isNumpadShapeV() { return getNumpadShape() == NUMPAD_SHAPE_V; }

	public int getSettingsFontSize() {
		int defaultSize = DeviceInfo.IS_QIN_F21 || DeviceInfo.IS_LG_X100S ? FONT_SIZE_LARGE : FONT_SIZE_DEFAULT;
		return getStringifiedInt("pref_font_size", defaultSize);
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
