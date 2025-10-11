package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class PrecalculateNavbarHeightSwitch extends SwitchWhenUIVisible implements ItemLayoutChangeReactive {
	public static final String NAME = "hack_precalculate_navbar_height_v3";


	public PrecalculateNavbarHeightSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	public PrecalculateNavbarHeightSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public PrecalculateNavbarHeightSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PrecalculateNavbarHeightSwitch(@NonNull Context context) {
		super(context);
		init(context);
	}

	private void init(@NonNull Context context) {
		setKey(NAME);
		setChecked(new SettingsStore(context).getPrecalculateNavbarHeight());
	}

	@Override
	public void onLayoutChange(int mainViewLayout) {
		setVisible(DeviceInfo.AT_LEAST_ANDROID_15 && mainViewLayout != SettingsStore.LAYOUT_STEALTH);
	}
}
