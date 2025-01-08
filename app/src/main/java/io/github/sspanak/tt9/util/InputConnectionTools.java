package io.github.sspanak.tt9.util;

import android.os.Build;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class InputConnectionTools {
	private static final String LOG_TAG = InputConnectionTools.class.getSimpleName();

	@Nullable private final InputConnection connection;

	@Nullable CompletableFuture<Boolean> future;
	@NonNull ExecutorService executor = Executors.newSingleThreadExecutor();
	private int connectionErrors = 0;
	private boolean isErrorReported = false;

	private CharSequence result;


	public InputConnectionTools(@Nullable InputConnection connection) {
		this.connection = connection;
	}


	public CharSequence getTextAfterCursor(int i, int ii) {
		return getTextNextToCursor(i, ii, true);
	}


	public CharSequence getTextBeforeCursor(int i, int ii) {
		return getTextNextToCursor(i, ii, false);
	}


	@Nullable
	private CharSequence getTextNextToCursor(int i, int ii, boolean after) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			getTextNextToCursorModern(i, ii, after);
		} else {
			getTextNextToCursorClassic(i, ii, after);
		}

		return result;
	}


	private void getTextNextToCursorClassic(int i, int ii, boolean after) {
		result = null;
		if (connection != null) {
			result = after ? connection.getTextAfterCursor(i, ii) : connection.getTextBeforeCursor(i, ii);
		}
	}


	/**
	 * getTextNextToCursorModern
	 * On some devices with Android >= 11, getTextBeforeCursor() sometimes takes too long to execute,
	 * blocking the UI thread and ultimately causing ANR. This method is a wrapper around
	 * getTextBeforeCursor() that terminates the operations after a certain timeout. Just in case we
	 * handle getTextAfterCursor() the same way.
	 */
	@RequiresApi(api = Build.VERSION_CODES.N)
	private void getTextNextToCursorModern(int i, int ii, boolean after) {
		// CompletableFuture is supported only in Android 24 and above, so we initialize it here.
		future = new CompletableFuture<>();

		// Start only the watchdog in a separate thread. If we start the main operation there too,
		// it will causes a deadlock, because main would be waiting for the thread, which will be waiting
		// for main to provide text.
		executor.submit(this::awaitTextResult);

		try {
			getTextNextToCursorClassic(i, ii, after);
			future.complete(true);
		} catch (Exception e) {
			connectionErrors++;
			logError("getStringBeforeCursor() failed " + connectionErrors + " times so far. Current error: " + e);
		}
	}


	@RequiresApi(api = Build.VERSION_CODES.N)
	private void awaitTextResult() {
		if (future == null) {
			logError("No future. Cannot call getStringBeforeCursor().");
			return;
		}

		try {
			if (!future.get(SettingsStore.INPUT_CONNECTION_OPERATIONS_TIMEOUT, TimeUnit.MILLISECONDS)) {
				logError("Future is not true. InputConnection.getTextBeforeCursor() probably failed.");
			}
		} catch (TimeoutException e) {
			connectionErrors++;
			logError(
				"getStringBeforeCursor() timed out after " + SettingsStore.INPUT_CONNECTION_OPERATIONS_TIMEOUT + "ms. " + connectionErrors + " errors so far."
			);
		} catch (InterruptedException | ExecutionException e) {
			connectionErrors++;
			logError("getStringBeforeCursor() failed " + connectionErrors + " times so far. Current error: " + e);
		}
	}


	private void logError(@NonNull String error) {
		Logger.e(LOG_TAG, error);
	}


	public boolean shouldReportTimeout() {
		if (!isErrorReported && connectionErrors > SettingsStore.INPUT_CONNECTION_ERRORS_MAX) {
			isErrorReported = true;
			return true;
		}
		return false;
	}
}
