package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class PreferenceCharsExtra extends AbstractPreferenceCharList {
	public static final String[] NAMES = {
		SettingsStore.CHARS_GROUP_0,
		SettingsStore.CHARS_AFTER_GROUP_0,
		SettingsStore.CHARS_GROUP_1,
		SettingsStore.CHARS_AFTER_GROUP_1
	};


	private char[] forbiddenChars;

	public PreferenceCharsExtra(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferenceCharsExtra(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferenceCharsExtra(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferenceCharsExtra(@NonNull Context context) { super(context); }

	@NonNull
	@Override
	protected String getChars() {
		return getSettings().getCharsExtra(language, getKey());
	}

	@NonNull
	@Override
	protected char[] getForbiddenChars() {
		if (forbiddenChars == null) {
			char[] mandatoryChars = getSettings().getMandatoryChars0(language);

			forbiddenChars = new char[mandatoryChars.length + SettingsStore.FORBIDDEN_CHARS_0.length];
			System.arraycopy(mandatoryChars, 0, forbiddenChars, 0, mandatoryChars.length);
			System.arraycopy(SettingsStore.FORBIDDEN_CHARS_0, 0, forbiddenChars, mandatoryChars.length, SettingsStore.FORBIDDEN_CHARS_0.length);
		}

		return forbiddenChars;
	}

	@NonNull
	@Override
	protected char[] getMandatoryChars() {
		return new char[0];
	}

	@Override
	public boolean validateCurrentChars() {
		String error = validateForbiddenChars();
		setError(error);
		return error.isEmpty();
	}

	@Override
	public void saveCurrentChars() {
		getSettings().saveCharsExtra(language, getKey(), currentChars);
	}
}
