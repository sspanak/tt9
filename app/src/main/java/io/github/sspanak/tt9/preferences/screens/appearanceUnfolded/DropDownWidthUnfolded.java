package io.github.sspanak.tt9.preferences.screens.appearanceUnfolded;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.screens.appearance.DropDownWidth;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class DropDownWidthUnfolded extends DropDownWidth {
	public static final String NAME = DropDownWidth.NAME + "_unfolded";

	public DropDownWidthUnfolded(@NonNull Context context) { super(context); }
	public DropDownWidthUnfolded(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownWidthUnfolded(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownWidthUnfolded(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected float getSettingsValue(@NonNull SettingsStore settings) {
		return settings.getWidthPercent(!DeviceInfo.isLandscapeOrientation(getContext()), false);
	}
}
