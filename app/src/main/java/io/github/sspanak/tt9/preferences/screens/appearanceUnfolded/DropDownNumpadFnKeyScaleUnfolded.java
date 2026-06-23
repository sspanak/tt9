package io.github.sspanak.tt9.preferences.screens.appearanceUnfolded;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.preferences.screens.appearance.DropDownNumpadFnKeyScale;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownNumpadFnKeyScaleUnfolded extends DropDownNumpadFnKeyScale {
	public static final String NAME = DropDownNumpadFnKeyScale.NAME + "_unfolded";

	public DropDownNumpadFnKeyScaleUnfolded(Context context) { super(context); }
	public DropDownNumpadFnKeyScaleUnfolded(Context context, AttributeSet attrs) { super(context, attrs); }
	public DropDownNumpadFnKeyScaleUnfolded(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownNumpadFnKeyScaleUnfolded(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected float getSettingsValue(@NonNull SettingsStore settings) {
		return settings.getNumpadFnKeyScale(false);
	}
}
