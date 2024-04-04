package io.github.sspanak.tt9.util;

public class DeviceInfo {
	public static boolean isQinF21() {
		return android.os.Build.MANUFACTURER.equals("DuoQin") && android.os.Build.MODEL.contains("F21");
	}
}
