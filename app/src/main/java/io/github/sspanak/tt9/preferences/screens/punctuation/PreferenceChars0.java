package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class PreferenceChars0 extends AbstractPreferenceCharList {
	public static final String NAME = "punctuation_order_special_chars";
	private char[] forbiddenChars;

	public PreferenceChars0(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferenceChars0(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferenceChars0(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferenceChars0(@NonNull Context context) { super(context); }


	@Override
	@NonNull
	protected String getChars() {
		return getSettings().getChars0(language);
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
		return SettingsStore.FORBIDDEN_CHARS_0;
	}


	@Override
	public boolean validateCurrentChars() {
		for (char c : getMandatoryChars()) {
			currentChars = currentChars.replace(String.valueOf(c), "");
		}

		String error = validateForbiddenChars();
		setError(error);

		return error.isEmpty();
	}


	@Override
	public void saveCurrentChars() {
		StringBuilder all = new StringBuilder();
		for (char c : getMandatoryChars()) {
			if (c == '\t') {
				if (settings.getIncludeTabInChars0(language)) {
					all.append(c);
				}
			} else if (c == '\n') {
				if (settings.getIncludeNewlineInChars0(language)) {
					all.append(c);
				}
			} else {
				all.append(c);
			}
		}
		all.append(currentChars);

		getSettings().saveChars0(language, all.toString());
	}
}
