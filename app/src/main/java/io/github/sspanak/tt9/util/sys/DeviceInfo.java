package io.github.sspanak.tt9.util.sys;

import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.os.Build;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DeviceInfo extends HardwareInfo {
	public static final boolean AT_LEAST_ANDROID_8 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
	public static final boolean AT_LEAST_ANDROID_8_1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1;
	public static final boolean AT_LEAST_ANDROID_9 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
	public static final boolean AT_LEAST_ANDROID_10 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
	public static final boolean AT_LEAST_ANDROID_11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
	public static final boolean AT_LEAST_ANDROID_12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
	public static final boolean AT_LEAST_ANDROID_13 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
	public static final boolean AT_LEAST_ANDROID_14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
	public static final boolean AT_LEAST_ANDROID_15 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM;
	public static final boolean AT_LEAST_ANDROID_16 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA;


	/**
	 * This isn't a reliable method because due to an Android, IMEs may not always receive orientation
	 * changes in the configuration. Use with caution.
	 */
	public static boolean isLandscapeOrientation(@NonNull Context context) {
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}


	/**
	 * The preferred method to check the orientation.
	 */
	public static boolean isLandscapeOrientation(@Nullable InputMethodService ime) {
		if (ime == null) {
			return false;
		}

		if (!AT_LEAST_ANDROID_11) {
			return ime.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		}

		WindowManager wm = ime.getSystemService(WindowManager.class);
		Rect bounds = wm.getCurrentWindowMetrics().getBounds();
		return bounds.width() > bounds.height();
	}


	public static boolean isZenMode(Context context) {
		if (context == null) {
			return false;
		}

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		return nm != null && nm.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL;
	}


	public static boolean isMuted(Context context) {
		if (context == null) {
			return false;
		}

		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		return am != null && am.getStreamVolume(AudioManager.STREAM_RING) == 0;
	}
}
