package io.github.sspanak.tt9.preferences.screens.keypad;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SwitchHapticFeedback extends SwitchPreferenceCompat {
	public static final String NAME = "pref_haptic_feedback";
	public static final String HACK_NAME = "hack_haptic_feedback_problematic";

	public static final boolean DEFAULT = true;
	public static final boolean HACK_DEFAULT = false;

	public SwitchHapticFeedback(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes); init(context);
	}

	public SwitchHapticFeedback(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr); init(context);
	}

	public SwitchHapticFeedback(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs); init(context);
	}

	public SwitchHapticFeedback(@NonNull Context context) {
		super(context); init(context);
	}

	private void init(@NonNull Context context) {
		setKey(NAME);
		setTitle(R.string.pref_haptic_feedback);

		if (new SettingsStore(context).getHapticFeedbackProblematic()) {
			setSummary(R.string.pref_haptic_feedback_problematic_summary);
		} else {
			setSummary(R.string.pref_haptic_feedback_summary);
		}

		setDefaultValue(DEFAULT);
		setChecked(getPersistedBoolean(DEFAULT));
	}
}
