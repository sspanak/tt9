package io.github.sspanak.tt9.preferences.screens.debug;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class DropDownLogLevel extends EnhancedDropDownPreference {
	public static final String NAME = "pref_log_level";

	public DropDownLogLevel(@NonNull Context context) { super(context); }
	public DropDownLogLevel(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownLogLevel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownLogLevel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }


	@Override
	protected void init(@NonNull Context c) {
		super.init(c);
		populate(new SettingsStore(c));
	}

	@Override
	protected boolean onChange(Preference preference, Object newKey) {
		Logger.setLevel(Integer.parseInt(newKey.toString()));
		return true;
	}

	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		add(String.valueOf(Log.VERBOSE), "Verbose");
		add(String.valueOf(Log.DEBUG), "Debug");
		add(String.valueOf(Log.INFO), "Info");
		add(String.valueOf(Log.WARN), "Warning");
		add(String.valueOf(Log.ERROR), "Error (default)");
		commitOptions();
		setValue(String.valueOf(Logger.LEVEL));
		preview();

		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}
}
