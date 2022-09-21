package io.github.sspanak.tt9;

import android.util.Log;

public class Logger {
	public static int LEVEL = Log.WARN;

	static public void v(String tag, String msgFormat, Object...args)  {
		if (LEVEL <= Log.VERBOSE) {
			Log.v(tag, String.format(msgFormat, args));
		}
	}

	static public void d(String tag, String msgFormat, Object...args)  {
		if (LEVEL <= Log.DEBUG) {
			Log.d(tag, String.format(msgFormat, args));
		}
	}

	static public void i(String tag,  String msgFormat, Object...args)  {
		if (LEVEL <= Log.INFO) {
			Log.i(tag, String.format(msgFormat, args));
		}
	}

	static public void w(String tag, String msgFormat, Object...args)  {
		if (LEVEL <= Log.WARN) {
			Log.w(tag, String.format(msgFormat, args));
		}
	}

	static public void e(String tag, String msgFormat, Object...args)  {
		if (LEVEL <= Log.ERROR) {
			Log.e(tag, String.format(msgFormat, args));
		}
	}
}
