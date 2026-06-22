package io.github.sspanak.tt9.preferences.screens.appearanceUnfolded;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.preferences.screens.appearance.DropDownSuggestionFontSize;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownSuggestionFontSizeUnfolded extends DropDownSuggestionFontSize {
	public static final String NAME = DropDownSuggestionFontSize.NAME + "_unfolded";

	public DropDownSuggestionFontSizeUnfolded(Context context) { super(context); }
	public DropDownSuggestionFontSizeUnfolded(Context context, AttributeSet attrs) { super(context, attrs); }
	public DropDownSuggestionFontSizeUnfolded(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownSuggestionFontSizeUnfolded(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected void initValue(@NonNull SettingsStore settings) {
		setValue(String.valueOf(settings.getSuggestionFontSizePercent(false)));
	}
}
