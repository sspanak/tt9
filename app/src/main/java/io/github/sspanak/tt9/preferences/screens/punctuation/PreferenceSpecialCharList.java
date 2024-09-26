package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.Logger;

public class PreferenceSpecialCharList extends AbstractPreferenceCharList {
	public static final String NAME = "punctuation_order_special_chars";

	public PreferenceSpecialCharList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public PreferenceSpecialCharList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public PreferenceSpecialCharList(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public PreferenceSpecialCharList(@NonNull Context context) {
		super(context);
	}

	@Override
	protected void onChange(String word) {
		Logger.d(getClass().getSimpleName(), "new special chars list: " + word);
	}

	@Override
	@NonNull
	protected String getChars(Language language) {
		return getSettings().getSpecialChars(language);
	}

	@NonNull
	@Override
	protected char[] getMandatoryChars() {
		return getSettings().mandatorySpecialChars;
	}
}
