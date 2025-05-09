package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class PreferenceSentencePunctuationList extends AbstractPreferenceCharList {
	public static final String NAME = "punctuation_order_sentence";

	public PreferenceSentencePunctuationList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferenceSentencePunctuationList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferenceSentencePunctuationList(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferenceSentencePunctuationList(@NonNull Context context) { super(context); }

	@Override
	@NonNull
	protected String getChars() {
		return getSettings().getPunctuation(language);
	}

	@NonNull
	@Override
	protected char[] getForbiddenChars() {
		return SettingsStore.FORBIDDEN_SPECIAL_CHARS;
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
		return super.validateMandatoryChars(getSettings().getMandatoryPunctuation(language));
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
		getSettings().savePunctuation(language, currentChars);
	}
}
