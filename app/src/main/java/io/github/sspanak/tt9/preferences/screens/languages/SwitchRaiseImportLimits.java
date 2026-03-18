package io.github.sspanak.tt9.preferences.screens.languages;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsAddedWords;

public class SwitchRaiseImportLimits extends SwitchPreferenceCompat {
	public static final String NAME = "pref_raise_custom_words_import_limits";
	public static final boolean DEFAULT = false;

	public SwitchRaiseImportLimits(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(context); }
	public SwitchRaiseImportLimits(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }
	public SwitchRaiseImportLimits(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(context); }
	public SwitchRaiseImportLimits(@NonNull Context context) { super(context); init(context); }

	private void init(@NonNull Context context) {
		setKey(NAME);
		setDefaultValue(DEFAULT);
		setTitle(R.string.dictionary_import_raise_import_limits);
		setSummary(context.getString(
			R.string.dictionary_import_raise_import_limits_summary,
			SettingsAddedWords.IMPORT_DEFAULT_MAX_FILE_LINES,
			SettingsAddedWords.IMPORT_RAISED_MAX_FILE_LINES,
			SettingsAddedWords.IMPORT_DEFAULT_MAX_WORDS,
			SettingsAddedWords.IMPORT_RAISED_MAX_WORDS
		));
	}
}
