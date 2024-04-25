package io.github.sspanak.tt9.hacks;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

public class DeviceInfo {
	public static boolean noTouchScreen(Context context) {
		return !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN);
	}

	public static boolean noKeyboard(Context context) {
		return
			context.getResources().getConfiguration().keyboard == Configuration.KEYBOARD_NOKEYS
			&& !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_STAR)
			&& !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POUND);
	}

	public static boolean isQinF21() {
		return Build.MANUFACTURER.equals("DuoQin") && Build.MODEL.contains("F21");
	}

	public static boolean isLgX100S() {
		return Build.MANUFACTURER.equals("LGE") && Build.MODEL.contains("X100S");
	}

	public static boolean isSonim() {
		return Build.MANUFACTURER.equals("Sonimtech");
	}

	public static boolean isSonimGen1(Context context) {
		return isSonim() && noTouchScreen(context) && Build.VERSION.SDK_INT < Build.VERSION_CODES.P;
	}

	public static boolean isSonimGen2(Context context) {
		return isSonim() && noTouchScreen(context) && Build.VERSION.SDK_INT <= Build.VERSION_CODES.R;
	}

	@NonNull
	@Override
	public String toString() {
		return "\"" + Build.MANUFACTURER + "\" " + "\"" + Build.MODEL + "\"";
	}
}
