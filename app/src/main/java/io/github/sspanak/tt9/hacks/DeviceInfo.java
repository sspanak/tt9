package io.github.sspanak.tt9.hacks;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;

public class DeviceInfo {
	private static Resources resources;

	private static Resources getResources(Context context) {
		if (resources == null) {
			resources = context.getResources();
		}
		return resources;
	}

	public static boolean isLandscapeOrientation(Context context) {
		return getResources(context).getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	public static int getNavigationBarHeight(Context context, boolean isLandscape) {
		Resources resources = getResources(context);

		// navBarMode = 0: 3-button, 1 = 2-button, 2 = gesture
		int resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
		int navBarMode = resourceId > 0 ? resources.getInteger(resourceId) : 0;

		int navBarHeight = resources.getDimensionPixelSize(R.dimen.android_navigation_bar_height);
		if (isLandscape) {
			return navBarMode == 0 ? 0 : navBarHeight;
		} else {
			return navBarHeight;
		}
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
		if (DeviceInfo.isXiaomi()) {
			return true;
		}

		Configuration configuration = getResources(context).getConfiguration();

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
