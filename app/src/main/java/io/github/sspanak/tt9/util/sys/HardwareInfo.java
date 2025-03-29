package io.github.sspanak.tt9.util.sys;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

public class HardwareInfo {
	public static final boolean IS_CAT_S22_FLIP = Build.MANUFACTURER.equals("Cat") && Build.MODEL.contains("S22");
	public static final boolean IS_LG_X100S = Build.MANUFACTURER.equals("LGE") && Build.MODEL.contains("X100S");
	public static final boolean IS_QIN_F21 = Build.MANUFACTURER.equals("DuoQin") && Build.MODEL.contains("F21");
	public static final boolean IS_SAMSUNG = Build.MANUFACTURER.equals("samsung") || Build.MANUFACTURER.equals("Samsung") || Build.MANUFACTURER.equals("SAMSUNG");
	public static final boolean IS_SONIM = Build.MANUFACTURER.equals("Sonimtech");
	public static final boolean IS_XIAOMI = Build.MANUFACTURER.equals("Xiaomi");

	private static Boolean NO_TOUCH_SCREEN = null;

	private static Resources resources;


	protected static Resources getResources(Context context) {
		if (resources == null) {
			resources = context.getResources();
		}
		return resources;
	}


	public static int getScreenWidth(Context context) {
		return getResources(context).getDisplayMetrics().widthPixels;
	}



	public static int getScreenHeight(Context context) {
		return getResources(context).getDisplayMetrics().heightPixels;
	}


	public static float getScreenHeightDp(Context context) {
		return getScreenHeight(context) / getResources(context).getDisplayMetrics().density;
	}


	public static float getScreenWidthDp(Context context) {
		return getScreenWidth(context) / getResources(context).getDisplayMetrics().density;
	}


	public static boolean noBackspaceKey() {
		return !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DEL) && !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_CLEAR);
	}


	public static boolean noKeyboard(Context context) {
		// all Xiaomi phones are only touchscreen, but some of them report they have a keyboard
		// See: https://github.com/sspanak/tt9/issues/549
		if (IS_XIAOMI) {
			return true;
		}

		Configuration configuration = getResources(context).getConfiguration();

		return
			(configuration.keyboard == Configuration.KEYBOARD_NOKEYS || configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
			&& !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_STAR)
			&& !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POUND);
	}


	public static boolean noTouchScreen(Context context) {
		if (NO_TOUCH_SCREEN == null) {
			NO_TOUCH_SCREEN = !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN);
		}
		return NO_TOUCH_SCREEN;
	}


	public static boolean isSonimGen2(Context context) {
		return IS_SONIM && DeviceInfo.AT_LEAST_ANDROID_11 && noTouchScreen(context);
	}


	@NonNull
	@Override
	public String toString() {
		return "\"" + Build.MANUFACTURER + "\" " + "\"" + Build.MODEL + "\"";
	}
}
