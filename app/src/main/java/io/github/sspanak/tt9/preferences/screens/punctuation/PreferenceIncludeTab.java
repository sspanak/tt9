package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class PreferenceIncludeTab extends AbstractIncludeCharsSwitch {
	public static final String NAME = "punctuation_order_include_tab";

	public PreferenceIncludeTab(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public PreferenceIncludeTab(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public PreferenceIncludeTab(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public PreferenceIncludeTab(@NonNull Context context) {
		super(context);
	}

	@Override
	boolean getChecked(@NonNull SettingsStore settings, @NonNull Language language) {
		return settings.getIncludeTabInChars0(language);
	}

	@Override
	void setChecked(@NonNull Language language, boolean checked) {
		new SettingsStore(getContext()).setIncludeTabInChars0(language, checked);
	}
}
