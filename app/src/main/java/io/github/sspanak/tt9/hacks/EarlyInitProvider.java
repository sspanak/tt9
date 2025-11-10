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
	private static final int SELF_KILL_TIMEOUT = 500; // ms
	private static final String LOG_TAG = EarlyInitProvider.class.getName();

	@Override
	public boolean onCreate() {
		final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> crashHandler(defaultHandler, thread, throwable));
		scheduleRemoveCrashHandler(defaultHandler);
		return true;
	}


	private void scheduleRemoveCrashHandler(Thread.UncaughtExceptionHandler defaultHandler) {
		new Handler(Looper.getMainLooper()).postDelayed(
			() -> Thread.setDefaultUncaughtExceptionHandler(defaultHandler),
			AUTO_REMOVE_TIMEOUT
		);
	}


	private void crashHandler(Thread.UncaughtExceptionHandler defaultHandler, Thread thread, Throwable throwable) {
		if (isPrivilegedOptionsError(throwable)) {
			Logger.e(LOG_TAG, "Caught privileged options exception!");
			stopService(getContext());
			stopSelf();
		} else if (defaultHandler != null) {
			Thread.setDefaultUncaughtExceptionHandler(defaultHandler);
			defaultHandler.uncaughtException(thread, throwable);
		} else {
			Logger.e(LOG_TAG, "Total startup failure. " + throwable.getMessage());
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


	/**
	 * Kills the IME service or any zombies left behind.
	 */
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


	/**
	 * Kills the current process after a short delay to allow the service to stop cleanly.
	 */
	private void stopSelf() {
		new Handler(Looper.getMainLooper()).postDelayed(() -> Process.killProcess(Process.myPid()), SELF_KILL_TIMEOUT);
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
