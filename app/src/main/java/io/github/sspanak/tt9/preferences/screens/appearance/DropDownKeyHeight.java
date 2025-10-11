package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownKeyHeight extends EnhancedDropDownPreference implements ItemLayoutChangeReactive {
	public static final String NAME = "pref_numpad_key_height";

	public DropDownKeyHeight(@NonNull Context context) { super(context); }
	public DropDownKeyHeight(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownKeyHeight(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownKeyHeight(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	public void onLayoutChange(int mainViewLayout) {
		setVisible(mainViewLayout == SettingsStore.LAYOUT_NUMPAD);
		setIconSpaceReserved(false);
	}

	@Override
	public DropDownKeyHeight populate(@NonNull SettingsStore settings) {
		int baseSize = settings.getNumpadKeyDefaultHeight();

		for (int i = 70; i <= 150; i += 5) {
			add(String.valueOf(Math.round(baseSize * i / 100.0)), i + " ％");
		}
		commitOptions();
		setValue(String.valueOf(settings.getNumpadKeyHeight()));
		onLayoutChange(settings.getMainViewLayout());

		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}
}
