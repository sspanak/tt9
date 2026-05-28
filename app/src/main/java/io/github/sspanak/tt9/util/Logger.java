package io.github.sspanak.tt9.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import io.github.sspanak.tt9.BuildConfig;

public class Logger {
	public static final String TAG_PREFIX = "tt9/";
	public static int LEVEL = BuildConfig.DEBUG ? Log.DEBUG : Log.ERROR;

	public static boolean isDebugLevel() {
		return LEVEL <= Log.DEBUG;
	}

	public static boolean isVerboseLevel() {
		return LEVEL <= Log.VERBOSE;
	}

	public static void setLevel(int level) {
		if (level >= Log.VERBOSE && level <= Log.ASSERT) {
			LEVEL = level;
		}
	}

	public static void ex(@NonNull String tag, @Nullable String prefix, @NonNull Throwable e) {
		StringBuilder errorMsg = new StringBuilder(prefix == null ? "" : prefix + " ");
		errorMsg.append(e.getMessage()).append("\nStack trace:");
		Arrays.stream(e.getStackTrace()).forEach(element -> errorMsg.append("\n").append(element.toString()));
		Logger.e(tag, errorMsg.toString());
	}

	public static void v(String tag, String msg)  {
		if (LEVEL <= Log.VERBOSE) {
			Log.d(TAG_PREFIX + tag, msg);
		}
	}

	public static void d(String tag, String msg)  {
		if (LEVEL <= Log.DEBUG) {
			Log.d(TAG_PREFIX + tag, msg);
		}
	}

	public static void i(String tag,  String msg)  {
		if (LEVEL <= Log.INFO) {
			Log.i(TAG_PREFIX + tag, msg);
		}
	}

	public static void w(String tag, String msg)  {
		if (LEVEL <= Log.WARN) {
			Log.w(TAG_PREFIX + tag, msg);
		}
	}

	public static void e(String tag, String msg)  {
		if (LEVEL <= Log.ERROR) {
			Log.e(TAG_PREFIX + tag, msg);
		}
	}
}
