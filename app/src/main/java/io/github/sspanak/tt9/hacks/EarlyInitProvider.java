package io.github.sspanak.tt9.hacks;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.util.Logger;


/**
 * Attempts to catch IllegalStateException PrivilegedOptions errors during startup, and restart
 * the IME service cleanly to avoid system input method crashes.
 */
public class EarlyInitProvider extends ContentProvider {
	private static final int AUTO_REMOVE_TIMEOUT = 30_000; // ms
	private static final String LOG_TAG = EarlyInitProvider.class.getName();
	private static final int EXIT_CODE = 10;

	private volatile Context context;
	private volatile Thread.UncaughtExceptionHandler defaultHandler;


	@Override
	public boolean onCreate() {
		context = getContext();
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this::crashHandler);
		autoRemoveCrashHandler();
		return true;
	}


	private void autoRemoveCrashHandler() {
		new Handler(Looper.getMainLooper()).postDelayed(
			() -> Thread.setDefaultUncaughtExceptionHandler(defaultHandler),
			AUTO_REMOVE_TIMEOUT
		);
	}


	private void crashHandler(Thread t, Throwable throwable) {
		if (isPrivilegedOptionsError(throwable)) {
			Logger.e(LOG_TAG, "Caught privileged options exception!");

			context = context == null ? getContext() : context;
			stopService(context);
			Process.killProcess(Process.myPid());
			System.exit(EXIT_CODE);
		} else {
			Thread.setDefaultUncaughtExceptionHandler(defaultHandler);
			passThroughException(t, throwable);
		}
	}


	private boolean isPrivilegedOptionsError(Throwable t) {
		if (!(t instanceof IllegalStateException)) {
			return false;
		}

		for (StackTraceElement e : t.getStackTrace()) {
			if (e.getClassName().contains("InputMethodPrivilegedOperations") || e.getMethodName().contains("initializeInternal")) {
				return true;
			}
		}
		return t.getCause() != null && isPrivilegedOptionsError(t.getCause());
	}


	private void stopService(@Nullable Context context) {
		if (context == null) {
			Logger.e(LOG_TAG, "Could not send 'stopService' to main. Context is null.");
			return;
		}

		try {
			context.stopService(new Intent(context, TraditionalT9.class));
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Could not stop IME service privileged options error. " + e.getMessage());
		}
	}


	private void passThroughException(Thread t, Throwable e) {
		if (defaultHandler != null) {
			defaultHandler.uncaughtException(t, e);
		} else {
			Logger.e(LOG_TAG, "Total startup failure. " + e.getMessage());
		}
	}


	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) { return null; }

	@Override
	public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) { return 0; }

	@Override
	public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) { return 0; }

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) { return null; }

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) { return null; }
}
