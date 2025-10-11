package io.github.sspanak.tt9.preferences.screens.keypad;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownKeyPadDebounceTime extends EnhancedDropDownPreference {
	public static final String NAME = "pref_key_pad_debounce_time";

	public DropDownKeyPadDebounceTime(@NonNull Context context) { super(context); }
	public DropDownKeyPadDebounceTime(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownKeyPadDebounceTime(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownKeyPadDebounceTime(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		add(0, R.string.pref_hack_key_pad_debounce_off);

		int[] values = new int[] { 20, 30, 50, 75, 100, 150, 250, 350 };
		for (int value : values) {
			add(value, value + " ms");
		}
		commitOptions();

		setDefaultValue(String.valueOf(settings.getKeyPadDebounceTime()));

		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}
}
