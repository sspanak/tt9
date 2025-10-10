package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.items.TextInputPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

abstract class AbstractPreferenceCharList extends TextInputPreference {
	@NonNull protected String currentChars = "";
	protected Language language;
	private boolean isInitialized = false;
	protected static SettingsStore settings;


	AbstractPreferenceCharList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	AbstractPreferenceCharList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	AbstractPreferenceCharList(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	AbstractPreferenceCharList(@NonNull Context context) { super(context); }


	protected SettingsStore getSettings() {
		if (settings == null) {
			settings = new SettingsStore(getContext());
		}
		return settings;
	}


	@Override
	protected void onTextChange() {
		currentChars = text;
		validateCurrentChars();
	}


	void onLanguageChange(Language language) {
		this.language = language;

		String all = getChars();
		char[] mandatory = getMandatoryChars();
		StringBuilder optional = new StringBuilder();

		// hide all mandatory characters
		for (int i = 0; i < all.length(); i++) {
			char c = all.charAt(i);

			boolean isMandatory = false;
			for (char m : mandatory) {
				if (m == c) {
					isMandatory = true;
					break;
				}
			}

			if (!isMandatory) {
				optional.append(c);
			}
		}

		setError(null);
		setText(currentChars = optional.toString());
	}


	protected String validateForbiddenChars() {
		StringBuilder forbiddenCharList = new StringBuilder();

		for (char c : getForbiddenChars()) {
			if (currentChars.indexOf(c) != -1) {
				forbiddenCharList.append(" ").append(getCharName(c)).append(",");
			}
		}

		if (forbiddenCharList.length() == 0) {
			return "";
		}

		String chars = forbiddenCharList.substring(0, forbiddenCharList.length() - 1);
		int message = chars.length() > 2 ? R.string.punctuation_order_forbidden_chars : R.string.punctuation_order_forbidden_char;
		return getContext().getString(message, chars);
	}


	protected String validateMandatoryChars() {
		return validateMandatoryChars(getMandatoryChars());
	}


	protected String validateMandatoryChars(char[] mandatoryChars) {
		StringBuilder missingCharList = new StringBuilder();

		for (char c : mandatoryChars) {
			if (currentChars.indexOf(c) == -1) {
				missingCharList.append(" ").append(getCharName(c)).append(",");
			}
		}

		if (missingCharList.length() == 0) {
			return "";
		}

		int message = missingCharList.length() == 3 ? R.string.punctuation_order_mandatory_char_missing : R.string.punctuation_order_mandatory_chars_missing;
		String chars = missingCharList.substring(0, missingCharList.length() - 1);
		return getContext().getString(message, chars);
	}


	private String getCharName(char c) {
		return switch (c) {
			case '\n' -> getContext().getString(R.string.char_newline);
			case ' ' -> getContext().getString(R.string.char_space);
			default -> String.valueOf(c);
		};
	}


	@NonNull abstract protected String getChars();
	@NonNull abstract protected char[] getForbiddenChars();
	@NonNull abstract protected char[] getMandatoryChars();
	abstract public boolean validateCurrentChars();
	abstract public void saveCurrentChars();
}
