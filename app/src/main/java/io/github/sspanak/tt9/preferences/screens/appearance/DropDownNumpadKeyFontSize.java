package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownNumpadKeyFontSize extends EnhancedDropDownPreference implements ItemLayoutChangeReactive{
	public static final String NAME = "pref_numpad_key_font_size";

	public DropDownNumpadKeyFontSize(@NonNull Context context) { super(context); }
	public DropDownNumpadKeyFontSize(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownNumpadKeyFontSize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownNumpadKeyFontSize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		commitPercentRange(80, 130, 5);
		setValue(String.valueOf(settings.getNumpadKeyFontSizePercent()));
		onLayoutChange(settings.getMainViewLayout());

		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}

	public void onLayoutChange(int mainViewLayout) {
		setVisible(mainViewLayout == SettingsStore.LAYOUT_CLASSIC || mainViewLayout == SettingsStore.LAYOUT_NUMPAD);
		setIconSpaceReserved(false);
	}
}
