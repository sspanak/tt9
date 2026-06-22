package io.github.sspanak.tt9.preferences.screens.appearanceUnfolded;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.preferences.screens.appearance.DropDownKeyHeight;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownKeyHeightUnfolded extends DropDownKeyHeight {
	public static final String NAME = DropDownKeyHeight.NAME + "_unfolded";

	public DropDownKeyHeightUnfolded(Context context) { super(context); }
	public DropDownKeyHeightUnfolded(Context context, AttributeSet attrs) { super(context, attrs); }
	public DropDownKeyHeightUnfolded(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownKeyHeightUnfolded(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected void initValue(@NonNull SettingsStore settings) {
		setValue(String.valueOf(settings.getNumpadKeyHeight(false)));
	}
}
