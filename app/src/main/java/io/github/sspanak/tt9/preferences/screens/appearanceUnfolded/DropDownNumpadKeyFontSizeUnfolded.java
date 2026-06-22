package io.github.sspanak.tt9.preferences.screens.appearanceUnfolded;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.preferences.screens.appearance.DropDownNumpadKeyFontSize;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownNumpadKeyFontSizeUnfolded extends DropDownNumpadKeyFontSize {
	public static final String NAME = DropDownNumpadKeyFontSize.NAME + "_unfolded";

	public DropDownNumpadKeyFontSizeUnfolded(android.content.Context context) { super(context); }
	public DropDownNumpadKeyFontSizeUnfolded(android.content.Context context, android.util.AttributeSet attrs) { super(context, attrs); }
	public DropDownNumpadKeyFontSizeUnfolded(android.content.Context context, android.util.AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownNumpadKeyFontSizeUnfolded(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected void initValue(@NonNull SettingsStore settings) {
		setValue(String.valueOf(settings.getNumpadKeyFontSizePercent(false)));
	}
}
