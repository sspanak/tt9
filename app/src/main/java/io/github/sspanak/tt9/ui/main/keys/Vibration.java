package io.github.sspanak.tt9.ui.main.keys;

import android.os.Build;
import android.view.HapticFeedbackConstants;

class Vibration {
	static int getNoVibration() {
		return -1;
	}

	static int getPressVibration(SoftKey key) {
		return key instanceof SoftNumberKey ? HapticFeedbackConstants.KEYBOARD_TAP : HapticFeedbackConstants.VIRTUAL_KEY;
	}

	static int getHoldVibration() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return HapticFeedbackConstants.CONFIRM;
		} else {
			return HapticFeedbackConstants.VIRTUAL_KEY;
		}
	}


	static int getReleaseVibration() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
			return HapticFeedbackConstants.KEYBOARD_RELEASE;
		} else {
			return HapticFeedbackConstants.VIRTUAL_KEY;
		}
	}
}
