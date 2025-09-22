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

public class DarkThemePreference extends DropDownPreference {
	public static final String NAME = "pref_theme";

	private final LinkedHashMap<String, String> options = new LinkedHashMap<>();

	public DarkThemePreference(@NonNull Context context) {
		super(context);
		init(context);
	}

	public DarkThemePreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DarkThemePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public DarkThemePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(Context context) {
		populate(context);
		setOnPreferenceChangeListener(this::onChange);
	}

	private void populate(Context context) {
		setKey(NAME);
		setTitle(R.string.pref_dark_theme);

		if (options.isEmpty()) {
			options.put(String.valueOf(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM), context.getString(R.string.pref_dark_theme_auto));
			options.put(String.valueOf(AppCompatDelegate.MODE_NIGHT_NO), context.getString(R.string.pref_dark_theme_no));
			options.put(String.valueOf(AppCompatDelegate.MODE_NIGHT_YES), context.getString(R.string.pref_dark_theme_yes));
		}

		setEntries(options.values().toArray(new CharSequence[0]));
		setEntryValues(options.keySet().toArray(new CharSequence[0]));

		String value = options.get(String.valueOf(new SettingsStore(context).getTheme()));
		if (value != null) {
			setValue(value);
			setSummary(value);
		} else {
			setValue(String.valueOf(-1));
			setSummary(R.string.key_none);
		}
	}

	protected boolean onChange(Preference preference, Object newKey) {
		AppCompatDelegate.setDefaultNightMode(Integer.parseInt(newKey.toString()));
		String value = options.get(newKey.toString());
		setSummary(value == null ? getContext().getString(R.string.key_none) : value);
		return true;
	}
}
