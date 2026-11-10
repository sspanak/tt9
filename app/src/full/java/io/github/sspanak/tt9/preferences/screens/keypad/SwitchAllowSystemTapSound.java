package io.github.sspanak.tt9.preferences.screens.keypad;


import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;

public class SwitchAllowSystemTapSound extends SwitchPreferenceCompat {
	public static final String NAME = "pref_allow_system_tap_sound";
	public static final boolean DEFAULT = false;

	public SwitchAllowSystemTapSound(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes); init(context);
	}

	public SwitchAllowSystemTapSound(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr); init(context);
	}

	public SwitchAllowSystemTapSound(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs); init(context);
	}

	public SwitchAllowSystemTapSound(@NonNull Context context) {
		super(context); init(context);
	}

	private void init(@NonNull Context context) {
		setKey(NAME);
		setTitle(R.string.pref_allow_system_tap_sound);
		setSummary(R.string.pref_allow_system_tap_sound_summary);

		setDefaultValue(DEFAULT);
		setChecked(getPersistedBoolean(DEFAULT));
	}
}
