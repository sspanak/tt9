package io.github.sspanak.tt9.preferences.screens.languages;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class QuickSwitchLanguagePreference extends SwitchPreferenceCompat {
	public QuickSwitchLanguagePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	public QuickSwitchLanguagePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public QuickSwitchLanguagePreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public QuickSwitchLanguagePreference(@NonNull Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		setDefaultValue(true);
		setKey("pref_quick_switch_language");
		setTitle(R.string.pref_quick_switch_language);
		setSummary(R.string.pref_quick_switch_language_summary);
		setVisible(!new SettingsStore(context).isMainLayoutStealth());
	}
}
