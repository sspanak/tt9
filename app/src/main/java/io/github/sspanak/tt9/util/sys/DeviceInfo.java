package io.github.sspanak.tt9.util.sys;

import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DeviceInfo extends HardwareInfo {
	public static final boolean AT_LEAST_ANDROID_6 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
	public static final boolean AT_LEAST_ANDROID_7 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
	public static final boolean AT_LEAST_ANDROID_8 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
	public static final boolean AT_LEAST_ANDROID_8_1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1;
	public static final boolean AT_LEAST_ANDROID_9 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
	public static final boolean AT_LEAST_ANDROID_10 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
	public static final boolean AT_LEAST_ANDROID_11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
	public static final boolean AT_LEAST_ANDROID_12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
	public static final boolean AT_LEAST_ANDROID_13 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
	public static final boolean AT_LEAST_ANDROID_14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
	public static final boolean AT_LEAST_ANDROID_15 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM;


	public static boolean isLandscapeOrientation(Context context) {
		return getResources(context).getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}


	public static boolean isZenMode(Context context) {
		if (context == null || !AT_LEAST_ANDROID_6) {
			return false;
		}

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		return nm != null && nm.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL;
	}


	public static boolean isMuted(Context context) {
		if (context == null || !AT_LEAST_ANDROID_6) {
			return false;
		}

		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		return am != null && am.getStreamVolume(AudioManager.STREAM_RING) == 0;
	}


	public static int getNavigationBarHeight(@NonNull Context context, @NonNull SettingsStore settings, boolean isLandscape) {
		if (!settings.getPrecalculateNavbarHeight()) {
			return 0;
		}

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
}
