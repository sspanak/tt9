package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownNumpadFnKeyScale extends EnhancedDropDownPreference implements ItemLayoutChangeReactive {
	public static final String NAME = "pref_numpad_fn_key_width";

	public DropDownNumpadFnKeyScale(@NonNull Context context) { super(context); }
	public DropDownNumpadFnKeyScale(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownNumpadFnKeyScale(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownNumpadFnKeyScale(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	public DropDownNumpadFnKeyScale populate(@NonNull SettingsStore settings) {
		add("1", "100 ％");
		add("0.85", "115 ％");
		add("0.75", "125 ％");
		add("0.675", "135 ％");
		add("0.576", "145 ％");
		add("0.477", "155 ％"); // whatever...
		commitOptions();

		setValue(getClosestOption(settings.getNumpadFnKeyScale(), values));
		onLayoutChange(settings.getMainViewLayout());

		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}

	private String getClosestOption(float value, LinkedHashMap<String, String> options) {
		float minDiff = Float.MAX_VALUE;
		String closest = null;

		for (String key : options.keySet()) {
			float fKey = Float.parseFloat(key);
			float diff = Math.abs(value - fKey);

			if (diff < minDiff) {
				minDiff = diff;
				closest = key;
			}
		}

		return closest;
	}

	public void onLayoutChange(int mainViewLayout) {
		setVisible(mainViewLayout == SettingsStore.LAYOUT_NUMPAD);
		setIconSpaceReserved(false);
	}
}
