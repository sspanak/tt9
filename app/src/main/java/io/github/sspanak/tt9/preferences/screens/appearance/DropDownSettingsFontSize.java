package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class DropDownSettingsFontSize extends EnhancedDropDownPreference {
	public static final String NAME = "pref_font_size";
	@Nullable private AppearanceScreen screen;

	public DropDownSettingsFontSize(@NonNull Context context) { super(context); }
	public DropDownSettingsFontSize(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownSettingsFontSize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownSettingsFontSize(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }


	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			setVisible(false);
			return this;
		}

		add(SettingsStore.FONT_SIZE_DEFAULT, R.string.pref_font_size_default);
		add(SettingsStore.FONT_SIZE_LARGE, R.string.pref_font_size_large);
		commitOptions();
		setValue(String.valueOf(new SettingsStore(getContext()).getSettingsFontSize()));

		return this;
	}

	void setScreen(@Nullable AppearanceScreen screen) {
		this.screen = screen;
	}

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected boolean onChange(Preference preference, Object newSize) {
		if (screen != null) {
			screen.resetFontSize(true);
			return true;
		}

		return false;
	}
}
