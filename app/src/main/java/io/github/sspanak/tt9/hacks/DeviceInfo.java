package io.github.sspanak.tt9.hacks;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

public class DeviceInfo {
	public static boolean isLandscapeOrientation(Context context) {
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	public static boolean noBackspaceKey() {
		return !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DEL) && !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_CLEAR);
	}

	public static boolean noKeyboard(Context context) {
		// all Xiaomi phones are only touchscreen, but some of them report they have a keyboard
		// See: https://github.com/sspanak/tt9/issues/549
		if (DeviceInfo.isXiaomi()) {
			return true;
		}

		Configuration configuration = context.getResources().getConfiguration();

		return
			(configuration.keyboard == Configuration.KEYBOARD_NOKEYS || configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
			&& !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_STAR)
			&& !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POUND);
	}

	public static boolean noTouchScreen(Context context) {
		return !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN);
	}

	public static boolean isCatS22Flip() {
		return Build.MANUFACTURER.equals("Cat") && Build.MODEL.contains("S22");
	}

	public static boolean isLgX100S() {
		return Build.MANUFACTURER.equals("LGE") && Build.MODEL.contains("X100S");
	}

	public static boolean isQinF21() {
		return Build.MANUFACTURER.equals("DuoQin") && Build.MODEL.contains("F21");
	}

	public static boolean isSonim() {
		return Build.MANUFACTURER.equals("Sonimtech");
	}

	public static boolean isSonimGen2(Context context) {
		return isSonim() && Build.VERSION.SDK_INT == Build.VERSION_CODES.R && noTouchScreen(context);
	}

	public static boolean isXiaomi() {
		return Build.MANUFACTURER.equals("Xiaomi");
	}

	@NonNull
	@Override
	public String toString() {
		return "\"" + Build.MANUFACTURER + "\" " + "\"" + Build.MODEL + "\"";
	}
}
