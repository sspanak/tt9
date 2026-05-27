package io.github.sspanak.tt9.preferences.screens.keychars;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.Text;

public class PreferenceChars2to9 extends AbstractPreferenceCharList {
	public static final String NAME_PREFIX = "extra_chars_";
	public static final String[] NAMES = {
		NAME_PREFIX + 2,
		NAME_PREFIX + 3,
		NAME_PREFIX + 4,
		NAME_PREFIX + 5,
		NAME_PREFIX + 6,
		NAME_PREFIX + 7,
		NAME_PREFIX + 8,
		NAME_PREFIX + 9
	};

	public PreferenceChars2to9(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		setTitleForSelectedLanguage(context);
	}

	public PreferenceChars2to9(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setTitleForSelectedLanguage(context);
	}

	public PreferenceChars2to9(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		setTitleForSelectedLanguage(context);
	}

	public PreferenceChars2to9(@NonNull Context context) {
		super(context);
		setTitleForSelectedLanguage(context);
	}


	private void setTitleForSelectedLanguage(@NonNull Context context) {
		setTitle(context.getString(R.string.key_key) + " #" + getNumber());
	}


	private int getNumber() {
		String key = getKey();
		if (key == null || !key.startsWith(NAME_PREFIX)) {
			return -1;
		}

		try {
			int n = Integer.parseInt(key.substring(NAME_PREFIX.length()));
			return (n >= 2 && n <= 9) ? n : -1;
		} catch (NumberFormatException e) {
			return -1;
		}
	}


	@Override
	void onLanguageChange(Language language) {
		super.onLanguageChange(language);
		setEnabled(language != null && !language.isTranscribed());
		setTitleForSelectedLanguage(getContext());
	}


	@NonNull
	@Override
	protected String getChars() {
		return getSettings().getCharsExtra(language, getKey());
	}


	@NonNull
	@Override
	protected char[] getForbiddenChars() {
		return new char[0];
	}


	@NonNull
	@Override
	protected char[] getMandatoryChars() {
		return new char[0];
	}


	@Override
	public boolean validateCurrentChars() {
		String error = "";

		if (!new Text(currentChars).isAlphabetic()) {
			error = getContext().getString(R.string.key_chars_error_only_letters_allowed);
		} else if (Text.containsRepeatingChars(currentChars)) {
			error = getContext().getString(R.string.key_chars_error_repeating_letters_not_allowed);
		}

		setError(error);
		return error.isEmpty();
	}


	@Override
	public void saveCurrentChars() {
		getSettings().saveCharsExtra(language, getKey(), currentChars);
	}
}
