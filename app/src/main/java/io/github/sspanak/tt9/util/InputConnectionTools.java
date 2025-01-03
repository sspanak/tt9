package io.github.sspanak.tt9.util;

import android.os.Build;
import android.view.inputmethod.InputConnection;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class InputConnectionTools {
	@Nullable private final InputConnection connection;
	private int connectionErrors;
	private boolean isErrorReported;


	public InputConnectionTools(@Nullable InputConnection connection) {
		this.connection = connection;
		connectionErrors = 0;
		isErrorReported = false;
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
			return getTextNextToCursorModern(i, ii, after);
		} else {
			return getTextNextToCursorClassic(i, ii, after);
		}
	}


	private CharSequence getTextNextToCursorClassic(int i, int ii, boolean after) {
		if (connection == null) {
			return null;
		}

		return after ? connection.getTextAfterCursor(i, ii) : connection.getTextBeforeCursor(i, ii);
	}


	/**
	 * getTextNextToCursorModern
	 * On some devices with Android >= 11, getTextBeforeCursor() sometimes takes too long to execute,
	 * blocking the UI thread and ultimately causing ANR. This method is a wrapper around
	 * getTextBeforeCursor() that terminates the operations after a certain timeout. Just in case we
	 * handle getTextAfterCursor() the same way.
	 */
	@Nullable
	@RequiresApi(api = Build.VERSION_CODES.N)
	private CharSequence getTextNextToCursorModern(int i, int ii, boolean after) {
		CompletableFuture<CharSequence> future = CompletableFuture.supplyAsync(
			() -> getTextNextToCursorClassic(i, ii, after)
		);

		try {
			return future.get(SettingsStore.INPUT_CONNECTION_OPERATIONS_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			connectionErrors++;
			Logger.e(
				getClass().getSimpleName(),
				"getStringBeforeCursor() timed out after " + SettingsStore.INPUT_CONNECTION_OPERATIONS_TIMEOUT + "ms. " + connectionErrors + " errors so far."
			);
			return null;
		} catch (InterruptedException | ExecutionException e) {
			connectionErrors++;
			Logger.e(getClass().getSimpleName(), "getStringBeforeCursor() failed " + connectionErrors + " times so far. Current error: " + e);
			return null;
		}
	}

	public boolean shouldReportTimeout() {
		if (!isErrorReported && connectionErrors > SettingsStore.INPUT_CONNECTION_ERRORS_MAX) {
			isErrorReported = true;
			return true;
		}
		return false;
	}
}
