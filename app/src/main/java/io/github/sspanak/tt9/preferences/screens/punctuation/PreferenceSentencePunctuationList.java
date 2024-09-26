package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.Logger;

public class PreferenceSentencePunctuationList extends AbstractPreferenceCharList {
	public static final String NAME = "punctuation_order_sentence";

	public PreferenceSentencePunctuationList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public PreferenceSentencePunctuationList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public PreferenceSentencePunctuationList(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public PreferenceSentencePunctuationList(@NonNull Context context) {
		super(context);
	}

	@Override
	protected void onChange(String word) {
		Logger.d(getClass().getSimpleName(), "new punctuation list: " + word);
	}

	@Override
	@NonNull
	protected String getChars(Language language) {
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
}
