package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.items.ItemTextInput;
import io.github.sspanak.tt9.util.Logger;

public class PreferenceSentencePunctuationList extends ItemTextInput {
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

	void onLanguageChanged(@Nullable String newLanguageId) {
		Logger.d(getClass().getSimpleName(), "new language: " + newLanguageId);
	}
}
