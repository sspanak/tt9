package io.github.sspanak.tt9;

import android.util.Log;

public class Logger {
	public static int LEVEL = Log.ERROR;

	static public void v(String tag, String msg)  {
		if (LEVEL <= Log.VERBOSE) {
			Log.v(tag, msg);
		}
	}

	static public void d(String tag, String msg)  {
		if (LEVEL <= Log.DEBUG) {
			Log.d(tag, msg);
		}
	}

	static public void i(String tag,  String msg)  {
		if (LEVEL <= Log.INFO) {
			Log.i(tag, msg);
		}
	}

	static public void w(String tag, String msg)  {
		if (LEVEL <= Log.WARN) {
			Log.w(tag, msg);
		}
	}

	static public void e(String tag, String msg)  {
		if (LEVEL <= Log.ERROR) {
			Log.e(tag, msg);
		}
	}
}
