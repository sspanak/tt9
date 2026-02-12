package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class DropDownBottomPaddingPortrait extends EnhancedDropDownPreference implements ItemLayoutChangeReactive {
	public static final String NAME = "pref_bottom_padding_portrait";
	private static final int DEFAULT_API_35 = 48;
	public static final int DEFAULT = DeviceInfo.AT_LEAST_ANDROID_15 ? DEFAULT_API_35 : 0;

	public DropDownBottomPaddingPortrait(@NonNull Context context) { super(context); }
	public DropDownBottomPaddingPortrait(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownBottomPaddingPortrait(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownBottomPaddingPortrait(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		commitRange(0, DEFAULT + DEFAULT_API_35, 8, " dp");
		setValue(String.valueOf(settings.getBottomPaddingPortrait()));
		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	public void onLayoutChange(int mainViewLayout) {
		setVisible(mainViewLayout != SettingsStore.LAYOUT_STEALTH);
	}
}
