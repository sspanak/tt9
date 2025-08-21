package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SwitchWhenUIVisible extends SwitchPreferenceCompat implements ItemLayoutChangeReactive {
	public SwitchWhenUIVisible(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public SwitchWhenUIVisible(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public SwitchWhenUIVisible(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public SwitchWhenUIVisible(@NonNull Context context) { super(context); }

	@Override
	public void onAttached() {
		super.onAttached();
		onLayoutChange(new SettingsStore(getContext()).getMainViewLayout());
	}

	@Override
	public void onLayoutChange(int mainViewLayout) {
		setVisible(mainViewLayout != SettingsStore.LAYOUT_STEALTH);
		setIconSpaceReserved(false);
	}
}
