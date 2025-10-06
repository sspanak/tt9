package io.github.sspanak.tt9.ime.helpers;

import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;

public class InputConnectionAsync {
	private static final String LOG_TAG = InputConnectionAsync.class.getSimpleName();
	private static final long DEFAULT_TIMEOUT_MS = 50;
	public static final String TIMEOUT_SENTINEL = "\u001F";

	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	private enum TaskType { GET_TEXT_BEFORE_CURSOR, GET_TEXT_AFTER_CURSOR, GET_EXTRACTED_TEXT }


	public static void destroy() {
		executor.shutdownNow();
	}


	public static CharSequence getTextBeforeCursor(InputConnection connection, int length, int flags) {
		return connection != null ? runAsync(() -> connection.getTextBeforeCursor(length, flags), TaskType.GET_TEXT_BEFORE_CURSOR) : null;
	}


	public static CharSequence getTextAfterCursor(InputConnection connection, int length, int flags) {
		return connection != null ? runAsync(() -> connection.getTextAfterCursor(length, flags), TaskType.GET_TEXT_AFTER_CURSOR) : null;
	}


	public static ExtractedText getExtractedText(InputConnection connection, ExtractedTextRequest request, int flags) {
		return connection != null ? runAsync(() -> connection.getExtractedText(request, flags), TaskType.GET_EXTRACTED_TEXT) : null;
	}


	private static <T> T getTimeout(TaskType taskType) {
		if (taskType == TaskType.GET_EXTRACTED_TEXT) {
			ExtractedText et = new ExtractedText();
			et.text = TIMEOUT_SENTINEL;
			return (T) et;
		} else {
			return (T) TIMEOUT_SENTINEL;
		}
	}


	private static <T> T runAsync(Callable<T> task, TaskType type) {
		Timer.start(LOG_TAG);
		Future<T> future = executor.submit(task);
		try {
			T retval = future.get(InputConnectionAsync.DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
			Logger.d(LOG_TAG, type.name() + " completed in: " + Timer.stop(LOG_TAG) + " ms");
			return retval;
		} catch (TimeoutException e) {
			future.cancel(true);
			Logger.w(LOG_TAG, type.name() + " timed out after: " + InputConnectionAsync.DEFAULT_TIMEOUT_MS + " ms");
			return getTimeout(type);
		} catch (Exception e) {
			Logger.w(LOG_TAG, type.name() + " failed after: " + Timer.stop(LOG_TAG) + " ms. " + e);
			return null;
		}
	}
}
