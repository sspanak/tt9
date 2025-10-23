package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownDarkTheme extends EnhancedDropDownPreference {
	public static final String NAME = "pref_theme";
	public static final int DEFAULT = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

	public DropDownDarkTheme(@NonNull Context context) { super(context); }
	public DropDownDarkTheme(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownDarkTheme(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownDarkTheme(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	protected void init(@NonNull Context context) {
		super.init(context);
		populate(new SettingsStore(context));
	}

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected int getDisplayTitle() {
		return R.string.pref_dark_theme;
	}

	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		add(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.pref_dark_theme_auto);
		add(AppCompatDelegate.MODE_NIGHT_NO, R.string.pref_dark_theme_no);
		add(AppCompatDelegate.MODE_NIGHT_YES, R.string.pref_dark_theme_yes);
		commitOptions();

		String value = String.valueOf(settings.getTheme());
		setValue(values.get(value) == null ? String.valueOf(DEFAULT) : value);
		preview();

		return this;
	}

	protected boolean onChange(Preference p, Object newKey) {
		super.onChange(p, newKey);
		// @todo: apply the colors the new way
//		AppCompatDelegate.setDefaultNightMode(Integer.parseInt(newKey.toString()));
		return true;
	}
}
