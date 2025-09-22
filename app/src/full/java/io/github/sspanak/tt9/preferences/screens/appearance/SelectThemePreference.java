package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SelectThemePreference extends DarkThemePreference {
	public SelectThemePreference(@NonNull Context context) { super(context); }
	public SelectThemePreference(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public SelectThemePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public SelectThemePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
}
