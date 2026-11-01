package io.github.sspanak.tt9.preferences.screens.keypad;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.KeypadCompatibilityService;
import io.github.sspanak.tt9.ui.UI;

public final class SwitchStarKeyCompatibility extends SwitchPreferenceCompat {
	public SwitchStarKeyCompatibility(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }

	public void refresh() {
		// Returning without enabling the Android service must not leave a misleading checked switch.
		if (isChecked() && Boolean.FALSE.equals(KeypadCompatibilityService.isEnabled(getContext()))) setChecked(false);
	}

	@Override
	protected void onClick() {
		super.onClick();
		if (!isChecked() || Boolean.TRUE.equals(KeypadCompatibilityService.isEnabled(getContext()))) return;
		Intent details = new Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS")
			.putExtra(Intent.EXTRA_COMPONENT_NAME, new ComponentName(getContext(), KeypadCompatibilityService.class).flattenToString())
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try { getContext().startActivity(details); }
		catch (ActivityNotFoundException | SecurityException exception) {
			try {
				getContext().startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			} catch (ActivityNotFoundException | SecurityException unavailable) {
				setChecked(false);
				UI.toastShortSingle(getContext(), R.string.pref_star_key_compatibility_summary);
			}
		}
	}
}
