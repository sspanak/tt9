package io.github.sspanak.tt9.preferences.screens.languages;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SwitchAutoAddWords extends SwitchPreferenceCompat {
	public static final String NAME = "pref_auto_add_words";
	public static final boolean DEFAULT = false;

	public SwitchAutoAddWords(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(context); }
	public SwitchAutoAddWords(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }
	public SwitchAutoAddWords(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(context); }
	public SwitchAutoAddWords(@NonNull Context context) { super(context); init(context); }

	private void init(@NonNull Context context) {
		setKey(NAME);
		setDefaultValue(DEFAULT);
		setTitle(R.string.add_word_auto);
		setSummary(R.string.add_word_auto_summary);
		setVisible(!new SettingsStore(context).isMainLayoutStealth());
	}
}
