package io.github.sspanak.tt9.preferences.screens.appearanceUnfolded;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.preferences.screens.appearance.DropDownBottomPaddingPortrait;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownBottomPaddingPortraitUnfolded extends DropDownBottomPaddingPortrait {
	public static final String NAME = DropDownBottomPaddingPortrait.NAME + "_unfolded";

	public DropDownBottomPaddingPortraitUnfolded(Context context) { super(context); }
	public DropDownBottomPaddingPortraitUnfolded(Context context, AttributeSet attrs) { super(context, attrs); }
	public DropDownBottomPaddingPortraitUnfolded(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownBottomPaddingPortraitUnfolded(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected void initValue(@NonNull SettingsStore settings) {
		setValue(String.valueOf(settings.getBottomPaddingPortrait(false)));
	}
}
