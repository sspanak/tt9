package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class SwitchKeyShadows extends SwitchPreferenceCompat {
	public static final String NAME = "pref_key_shadows";
	public static final boolean DEFAULT = !DeviceInfo.AT_LEAST_ANDROID_12;

	public SwitchKeyShadows(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	public SwitchKeyShadows(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public SwitchKeyShadows(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SwitchKeyShadows(@NonNull Context context) {
		super(context);
		init();
	}

	private void init() {
		setKey(NAME);
		setTitle(io.github.sspanak.tt9.R.string.pref_key_shadows);
		setChecked(getPersistedBoolean(DEFAULT));
	}
}
