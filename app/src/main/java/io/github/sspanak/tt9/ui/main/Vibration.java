package io.github.sspanak.tt9.ui.main;

import android.os.Build;
import android.view.HapticFeedbackConstants;

import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.ui.main.keys.SoftNumberKey;

public class Vibration {
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
}
