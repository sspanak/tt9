package io.github.sspanak.tt9.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SupremeExecutor {
	@Nullable private static ExecutorService executor;

	@NonNull
	synchronized public static ExecutorService get() {
		if (executor == null) {
			executor = Executors.newCachedThreadPool();
		}
		return executor;
	}

	public static Future<?> submit(@NonNull Runnable task) {
		return get().submit(task);
	}
}
