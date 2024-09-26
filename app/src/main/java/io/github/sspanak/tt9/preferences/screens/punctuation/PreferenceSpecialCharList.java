package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
	@NonNull
	protected String getChars() {
		return getSettings().getSpecialChars(language);
	}

	@NonNull
	@Override
	protected char[] getMandatoryChars() {
		return getSettings().mandatorySpecialChars;
	}

	@Override
	protected boolean validateCurrentChars() {
		StringBuilder validChars = new StringBuilder();

		for (char c : getSettings().mandatorySpecialChars) {
			if (currentChars.indexOf(c) == -1) {
				validChars.append(c);
			}
		}

		currentChars = validChars.toString();

		return true;
	}

	@Override
	protected void saveCurrentChars() {
		StringBuilder all = new StringBuilder();
		for (char c : getMandatoryChars()) {
			all.append(c);
		}
		all.append(currentChars);

		getSettings().saveSpecialChars(language, all.toString());
	}
}
