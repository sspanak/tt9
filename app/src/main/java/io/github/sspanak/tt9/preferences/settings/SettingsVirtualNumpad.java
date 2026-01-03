package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownKeyHeight;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownNumpadFnKeyScale;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownNumpadKeyFontSize;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownNumpadShape;
import io.github.sspanak.tt9.preferences.screens.appearance.SwitchLeftRightArrows;
import io.github.sspanak.tt9.preferences.screens.appearance.SwitchShowArrowsUpDown;
import io.github.sspanak.tt9.preferences.screens.fnKeyOrder.FnKeyOrderValidator;

public class SettingsVirtualNumpad extends SettingsCustomKeyActions {
	public final static int NUMPAD_SHAPE_SQUARE = 0;
	public final static int NUMPAD_SHAPE_V = 1;
	public final static int NUMPAD_SHAPE_LONG_SPACE = 2;

	public static final String DEFAULT_LFN_KEY_ORDER = "1234";
	public static final String DEFAULT_RFN_KEY_ORDER = "5678";

	public static final HashMap<Character, Integer> KEY_ORDER_MAP = new HashMap<>() {{
		put('1', R.id.soft_key_wrapper_1);
		put('2', R.id.soft_key_wrapper_2);
		put('3', R.id.soft_key_wrapper_3);
		put('4', R.id.soft_key_wrapper_4);
		put('5', R.id.soft_key_wrapper_5);
		put('6', R.id.soft_key_wrapper_6);
		put('7', R.id.soft_key_wrapper_7);
		put('8', R.id.soft_key_wrapper_8);
	}};

	SettingsVirtualNumpad(Context context) {
		super(context);
	}

	public boolean getArrowsLeftRight() {
		return prefs.getBoolean(SwitchLeftRightArrows.NAME, SwitchLeftRightArrows.DEFAULT);
	}

	public boolean getArrowsUpDown() {
		return prefs.getBoolean(SwitchShowArrowsUpDown.NAME, SwitchShowArrowsUpDown.DEFAULT);
	}

	public boolean getHardwareKeyVisualFeedback() {
		return prefs.getBoolean("pref_hardware_key_visual_feedback", false);
	}

	@NonNull public String getLfnKeyOrder() {
		return prefs.getString("pref_lfn_key_order", DEFAULT_LFN_KEY_ORDER);
	}

	@NonNull public String getRfnKeyOrder() {
		return prefs.getString("pref_rfn_key_order", DEFAULT_RFN_KEY_ORDER);
	}

	public FnKeyOrderValidator setFnKeyOrder(String left, String right) {
		FnKeyOrderValidator validator = new FnKeyOrderValidator(left, right);
		if (validator.validate()) {
			getPrefsEditor()
				.putString("pref_rfn_key_order", right)
				.putString("pref_lfn_key_order", left)
				.apply();
		}

		return validator;
	}

	public int getNumpadKeyDefaultHeight() {
		return context.getResources().getDimensionPixelSize(R.dimen.numpad_key_height);
	}

	public int getNumpadKeyHeight() {
		return getStringifiedInt(DropDownKeyHeight.NAME, getNumpadKeyDefaultHeight());
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
		return getStringifiedFloat(DropDownNumpadFnKeyScale.NAME, getNumpadFnKeyDefaultScale());
	}

	public int getNumpadKeyFontSizePercent() {
		return isMainLayoutLarge() ? getStringifiedInt(DropDownNumpadKeyFontSize.NAME, 100) : 100;
	}

	public int getNumpadShape() {
		return getStringifiedInt(DropDownNumpadShape.NAME, NUMPAD_SHAPE_SQUARE);
	}

	public boolean isNumpadShapeLongSpace() { return getNumpadShape() == NUMPAD_SHAPE_LONG_SPACE; }
	public boolean isNumpadShapeV() { return getNumpadShape() == NUMPAD_SHAPE_V; }


	public boolean getTutorialSeen() {
		if (isMainLayoutClassic()) {
			return prefs.getBoolean("pref_tutorial_classic_seen", false);
		} else if (isMainLayoutNumpad()) {
			return prefs.getBoolean("pref_tutorial_fn_keys_seen", false);
		} else {
			return false;
		}
	}


	public void setTutorialSeen() {
		if (isMainLayoutClassic()) {
			getPrefsEditor().putBoolean("pref_tutorial_classic_seen", true).apply();
		} else if (isMainLayoutNumpad()) {
			getPrefsEditor().putBoolean("pref_tutorial_fn_keys_seen", true).apply();
		}
	}
}
