package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class PreferenceChars1 extends AbstractPreferenceCharList {
	public static final String NAME = "punctuation_order_sentence";

	public PreferenceChars1(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferenceChars1(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferenceChars1(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferenceChars1(@NonNull Context context) { super(context); }

	@Override
	@NonNull
	protected String getChars() {
		return getSettings().getChars1(language);
	}

	@NonNull
	@Override
	protected char[] getForbiddenChars() {
		return SettingsStore.FORBIDDEN_CHARS_0;
	}

	/**
	 * Allow the user to rearrange all characters even the mandatory ones. We will verify if they
	 * are present upon saving.
	 */
	@NonNull
	@Override
	protected char[] getMandatoryChars() {
		 return new char[0];
	}

	@Override
	protected String validateMandatoryChars() {
		return super.validateMandatoryChars(getSettings().getMandatoryChars0(language));
	}

	public boolean validateCurrentChars() {
		if (currentChars.isEmpty()) {
			setError(getContext().getString(R.string.punctuation_order_cannot_be_empty));
			return false;
		}

		String forbiddenCharsError = validateForbiddenChars();
		String mandatoryCharsError = validateMandatoryChars();

		String separator = forbiddenCharsError.isEmpty() || mandatoryCharsError.isEmpty() ? "" : "\n";
		String error = forbiddenCharsError + separator + mandatoryCharsError;
		setError(error);

		return error.isEmpty();
	}

	@Override
	public void saveCurrentChars() {
		getSettings().saveChars1(language, currentChars);
	}
}
