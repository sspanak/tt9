package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;

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

	/**
	 * We want to all the user to rearrange all characters even the mandatory ones.
	 * We will verify if they are present upon saving.
	 */
	@NonNull
	@Override
	protected char[] getMandatoryChars() {
		 return new char[0];
	}

	protected boolean validateCurrentChars() {
		for (char c : getSettings().mandatoryPunctuation) {
			if (currentChars.indexOf(c) == -1) {
				setSummary(getContext().getString(R.string.punctuation_order_mandatory_char_missing, c));
				return false;
			}
		}

		setSummary("");
		return true;
	}

	@Override
	protected void saveCurrentChars() {
		getSettings().savePunctuation(language, currentChars);
	}
}
