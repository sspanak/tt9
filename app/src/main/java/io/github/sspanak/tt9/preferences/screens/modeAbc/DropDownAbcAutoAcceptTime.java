package io.github.sspanak.tt9.preferences.screens.modeAbc;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;


public class DropDownAbcAutoAcceptTime extends EnhancedDropDownPreference {
	public static final String NAME = "pref_abc_auto_accept_time";
	public static final int DEFAULT = 800;

	public DropDownAbcAutoAcceptTime(@NonNull Context context) { super(context); }
	public DropDownAbcAutoAcceptTime(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownAbcAutoAcceptTime(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownAbcAutoAcceptTime(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		add("-1", R.string.pref_abc_auto_accept_off);
		add("350", R.string.pref_abc_auto_accept_fastest);
		add("500", R.string.pref_abc_auto_accept_fast);
		add(DEFAULT, R.string.pref_abc_auto_accept_normal);
		add("1200", R.string.pref_abc_auto_accept_slow);
		commitOptions();
		setValue(String.valueOf(settings.getAbcAutoAcceptTimeout()));
		setDefaultValue(String.valueOf(DEFAULT));

		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}
}
