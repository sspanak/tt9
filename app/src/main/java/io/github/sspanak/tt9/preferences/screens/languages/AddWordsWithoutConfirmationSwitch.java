package io.github.sspanak.tt9.preferences.screens.languages;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class AddWordsWithoutConfirmationSwitch extends SwitchPreferenceCompat {
	public static final String NAME = "add_word_no_confirmation";

	public AddWordsWithoutConfirmationSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	public AddWordsWithoutConfirmationSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public AddWordsWithoutConfirmationSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AddWordsWithoutConfirmationSwitch(@NonNull Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		setKey(NAME);
		setTitle(R.string.add_word_no_confirmation);
		setVisible(!new SettingsStore(context).isMainLayoutStealth());
	}
}
