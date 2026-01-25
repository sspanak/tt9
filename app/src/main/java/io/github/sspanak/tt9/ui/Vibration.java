package io.github.sspanak.tt9.ui;

import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.keys.BaseClickableKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyNumber;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public record Vibration(@NonNull SettingsStore settings, @Nullable View view) {
	public static int getNoVibration() {
		return -1;
	}

	public static int getPressVibration(BaseClickableKey key) {
		return key instanceof SoftKeyNumber ? HapticFeedbackConstants.KEYBOARD_TAP : HapticFeedbackConstants.VIRTUAL_KEY;
	}

	public static int getHoldVibration() {
		if (DeviceInfo.AT_LEAST_ANDROID_11) {
			return HapticFeedbackConstants.CONFIRM;
		} else {
			return HapticFeedbackConstants.VIRTUAL_KEY;
		}
	}

	public static int getReleaseVibration() {
		if (DeviceInfo.AT_LEAST_ANDROID_8_1) {
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
