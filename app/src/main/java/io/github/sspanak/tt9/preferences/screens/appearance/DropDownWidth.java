package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownWidth extends EnhancedDropDownPreference implements ItemLayoutChangeReactive {
	public static final String NAME = "pref_numpad_width";

	public DropDownWidth(@NonNull Context context) { super(context); }
	public DropDownWidth(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownWidth(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownWidth(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	public void onLayoutChange(int mainViewLayout) {
		setEnabled(mainViewLayout != SettingsStore.LAYOUT_STEALTH);
	}

	@Override
	public DropDownWidth populate(@NonNull SettingsStore settings) {
		commitPercentRange(SettingsStore.MIN_WIDTH_PERCENT, 100, 5);

		float currentValue = settings.getWidthPercent();
		currentValue = Math.round(currentValue / 5f) * 5f;
		currentValue = Math.max(Math.min(currentValue, 100f), 50f);

		setValue(String.valueOf((int) currentValue));
		onLayoutChange(settings.getMainViewLayout());

		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}
}
