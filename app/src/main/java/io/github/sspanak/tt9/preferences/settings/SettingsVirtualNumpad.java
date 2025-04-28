package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.util.TypedValue;

import java.util.HashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.screens.fnKeyOrder.FnKeyOrderValidator;

public class SettingsVirtualNumpad extends SettingsUI {
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

	public boolean areArrowKeysHidden() {
		return !prefs.getBoolean("pref_arrow_keys_visible", true);
	}

	public String getLfnKeyOrder() {
		return prefs.getString("pref_lfn_key_order", DEFAULT_LFN_KEY_ORDER);
	}

	public String getRfnKeyOrder() {
		return prefs.getString("pref_rfn_key_order", DEFAULT_RFN_KEY_ORDER);
	}

	public FnKeyOrderValidator setFnKeyOrder(String left, String right) {
		FnKeyOrderValidator validator = new FnKeyOrderValidator(left, right);
		if (validator.validate()) {
			prefsEditor
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
}
