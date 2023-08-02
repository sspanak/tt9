package io.github.sspanak.tt9;

import android.util.Log;

public class Logger {
	public static final String TAG_PREFIX = "tt9/";
	public static int LEVEL = BuildConfig.DEBUG ? Log.DEBUG : Log.ERROR;

	public static boolean isDebugLevel() {
		return LEVEL == Log.DEBUG;
	}

	public static void setDebugLevel() {
		LEVEL = Log.DEBUG;
	}

	public static void setDefaultLevel() {
		LEVEL = Log.ERROR;
	}

	static public void v(String tag, String msg)  {
		if (LEVEL <= Log.VERBOSE) {
			Log.v(TAG_PREFIX + tag, msg);
		}
	}

	static public void d(String tag, String msg)  {
		if (LEVEL <= Log.DEBUG) {
			Log.d(TAG_PREFIX + tag, msg);
		}
	}

	static public void i(String tag,  String msg)  {
		if (LEVEL <= Log.INFO) {
			Log.i(TAG_PREFIX + tag, msg);
		}
	}

	static public void w(String tag, String msg)  {
		if (LEVEL <= Log.WARN) {
			Log.w(TAG_PREFIX + tag, msg);
		}
	}

	static public void e(String tag, String msg)  {
		if (LEVEL <= Log.ERROR) {
			Log.e(TAG_PREFIX + tag, msg);
		}
	}
}
