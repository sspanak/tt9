package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

abstract public class AbstractIncludeCharsSwitch extends SwitchPreferenceCompat {
	@Nullable private Language language = null;
	@Nullable private Runnable onChangeListener = null;

	public AbstractIncludeCharsSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		setOnPreferenceChangeListener(this::onSave);
	}

	public AbstractIncludeCharsSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setOnPreferenceChangeListener(this::onSave);
	}

	public AbstractIncludeCharsSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		setOnPreferenceChangeListener(this::onSave);
	}

	public AbstractIncludeCharsSwitch(@NonNull Context context) {
		super(context);
		setOnPreferenceChangeListener(this::onSave);
	}

	abstract boolean getChecked(@NonNull SettingsStore settings, @NonNull Language language);
	abstract void setChecked(@NonNull Language language, boolean checked);

	public void setLanguage(@NonNull SettingsStore settings, @NonNull Language language) {
		this.language = language;
		setChecked(getChecked(settings, language));
	}

	public void setOnChange(Runnable handler) {
		onChangeListener = handler;
	}

	private boolean onSave(Preference preference, Object newValue) {
		if (language == null) {
			return false;
		}

		boolean checked = (boolean) newValue;
		setChecked(language, checked);
		if (onChangeListener != null) {
			onChangeListener.run();
		}

		return true;
	}
}
