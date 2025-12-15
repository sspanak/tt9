package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownNumpadShape extends EnhancedDropDownPreference implements ItemLayoutChangeReactive {
	public static final String NAME = "pref_numpad_shape";

	public DropDownNumpadShape(@NonNull Context context) { super(context); }
	public DropDownNumpadShape(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownNumpadShape(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownNumpadShape(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	public DropDownNumpadShape populate(@NonNull SettingsStore settings) {
		add(SettingsStore.NUMPAD_SHAPE_SQUARE, R.string.pref_numpad_shape_square);
		add(SettingsStore.NUMPAD_SHAPE_V, R.string.pref_numpad_shape_v);
		add(SettingsStore.NUMPAD_SHAPE_LONG_SPACE, R.string.pref_numpad_shape_long_space);
		commitOptions();
		setValue(String.valueOf(settings.getNumpadShape()));
		onLayoutChange(settings.getMainViewLayout());

		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}

	public void onLayoutChange(int mainViewLayout) {
		setVisible(mainViewLayout == SettingsStore.LAYOUT_NUMPAD || mainViewLayout == SettingsStore.LAYOUT_CLASSIC);
		setIconSpaceReserved(false);
	}
}
