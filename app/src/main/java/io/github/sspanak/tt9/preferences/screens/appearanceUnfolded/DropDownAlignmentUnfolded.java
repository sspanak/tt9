package io.github.sspanak.tt9.preferences.screens.appearanceUnfolded;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.screens.appearance.DropDownAlignment;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownAlignmentUnfolded extends DropDownAlignment {
	public static final String NAME = DropDownAlignment.NAME + "_unfolded";

	public DropDownAlignmentUnfolded(@NonNull Context context) { super(context); }
	public DropDownAlignmentUnfolded(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownAlignmentUnfolded(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownAlignmentUnfolded(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected void initValue(@NonNull SettingsStore settings) {
		setValue(String.valueOf(settings.getAlignment(false)));
	}
}
