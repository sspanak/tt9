package io.github.sspanak.tt9.ui;

import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.ui.main.keys.SoftNumberKey;

public class Vibration {
	@NonNull private final SettingsStore settings;
	private final View view;

	public Vibration(@NonNull SettingsStore settings, View view) {
		this.settings = settings;
		this.view = view;
	}

	public static int getNoVibration() {
		return -1;
	}

	public static int getPressVibration(SoftKey key) {
		return key instanceof SoftNumberKey ? HapticFeedbackConstants.KEYBOARD_TAP : HapticFeedbackConstants.VIRTUAL_KEY;
	}

	public static int getHoldVibration() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return HapticFeedbackConstants.CONFIRM;
		} else {
			return HapticFeedbackConstants.VIRTUAL_KEY;
		}
	}

	public static int getReleaseVibration() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
			return HapticFeedbackConstants.KEYBOARD_RELEASE;
		} else {
			return HapticFeedbackConstants.VIRTUAL_KEY;
		}
	}

	public void vibrate(int vibrationType) {
		if (settings.getHapticFeedback() && view != null) {
			view.performHapticFeedback(vibrationType, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
		}
	}

	public void vibrate() {
		vibrate(getPressVibration(null));
	}
}
