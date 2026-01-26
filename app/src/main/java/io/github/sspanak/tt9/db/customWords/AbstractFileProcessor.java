package io.github.sspanak.tt9.db.customWords;

import android.app.Activity;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

abstract public class AbstractFileProcessor {
	protected Runnable failureHandler;
	protected Runnable startHandler;
	protected Consumer<String> successHandler;

	private Thread processThread;
	protected String statusMessage = "";

	public boolean isRunning() {
		return processThread != null && processThread.isAlive();
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	protected void sendFailure() {
		if (failureHandler != null) {
			failureHandler.run();
		}
	}

	protected void sendStart(@NonNull String message) {
		if (startHandler != null) {
			statusMessage = message;
			startHandler.run();
		}
	}

	public void setFailureHandler(Runnable handler) {
		failureHandler = handler;
	}

	public void setStartHandler(Runnable handler) {
		startHandler = handler;
	}

	public void setSuccessHandler(Consumer<String> handler) {
		successHandler = handler;
	}

	public boolean run(@NonNull Activity activity) {
		if (isRunning()) {
			return false;
		}

		processThread = new Thread(() -> runSync(activity));
		processThread.start();

		return true;
	}

	abstract protected void sendSuccess();
	abstract protected void runSync(Activity activity);
}
