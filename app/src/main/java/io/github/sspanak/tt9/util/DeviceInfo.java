package io.github.sspanak.tt9.util;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

public class DeviceInfo {
	public static boolean noTouchScreen(Context context) {
		return !context.getPackageManager().hasSystemFeature("android.hardware.touchscreen");
	}

	public static boolean isQinF21() {
		return Build.MANUFACTURER.equals("DuoQin") && Build.MODEL.contains("F21");
	}

	public static boolean isSonim() {
		return Build.MANUFACTURER.equals("Sonimtech");
	}

	@NonNull
	@Override
	public String toString() {
		return "\"" + Build.MANUFACTURER + "\" " + "\"" + Build.MODEL + "\"";
	}
}
